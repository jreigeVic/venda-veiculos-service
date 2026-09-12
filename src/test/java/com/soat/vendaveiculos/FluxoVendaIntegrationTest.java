package com.soat.vendaveiculos;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FluxoVendaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TOKEN = "test-token";

    @Test
    void endpointInternoDeveRejeitarSemToken() throws Exception {
        mockMvc.perform(post("/interno/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void fluxoCompletoDeCadastroVendaEPagamentoAprovado() throws Exception {
        UUID veiculoId = UUID.randomUUID();
        String payloadCriacao = """
                {
                  "id": "%s",
                  "marca": "Fiat",
                  "modelo": "Argo",
                  "ano": 2022,
                  "cor": "Prata",
                  "preco": 78900.00,
                  "estadoConservacao": "SEMINOVO"
                }
                """.formatted(veiculoId);

        mockMvc.perform(post("/interno/veiculos")
                        .header("X-Internal-Token", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadCriacao))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DISPONIVEL"));

        mockMvc.perform(get("/veiculos/a-venda"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(veiculoId.toString()));

        String payloadVenda = """
                {
                  "veiculoId": "%s",
                  "cpfComprador": "111.444.777-35",
                  "dataVenda": "2026-09-12"
                }
                """.formatted(veiculoId);

        String respostaVenda = mockMvc.perform(post("/vendas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadVenda))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusPagamento").value("PENDENTE"))
                .andReturn().getResponse().getContentAsString();

        JsonNode venda = objectMapper.readTree(respostaVenda);
        String codigoPagamento = venda.get("codigoPagamento").asText();

        mockMvc.perform(get("/veiculos/a-venda"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        String payloadWebhook = """
                { "codigoPagamento": "%s", "status": "APROVADO" }
                """.formatted(codigoPagamento);

        mockMvc.perform(post("/pagamentos/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadWebhook))
                .andExpect(status().isOk());

        mockMvc.perform(get("/veiculos/vendidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(veiculoId.toString()));

        mockMvc.perform(post("/pagamentos/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadWebhook))
                .andExpect(status().isOk());
    }

    @Test
    void deveRetornarConflitoAoVenderVeiculoIndisponivel() throws Exception {
        UUID veiculoId = UUID.randomUUID();
        String payloadCriacao = """
                {
                  "id": "%s",
                  "marca": "VW",
                  "modelo": "Nivus",
                  "ano": 2023,
                  "cor": "Branco",
                  "preco": 99900.00,
                  "estadoConservacao": "NOVO"
                }
                """.formatted(veiculoId);

        mockMvc.perform(post("/interno/veiculos")
                        .header("X-Internal-Token", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadCriacao))
                .andExpect(status().isCreated());

        String payloadEdicao = """
                {
                  "id": "%s",
                  "marca": "VW",
                  "modelo": "Nivus",
                  "ano": 2023,
                  "cor": "Preto",
                  "preco": 97900.00,
                  "estadoConservacao": "NOVO"
                }
                """.formatted(veiculoId);
        mockMvc.perform(put("/interno/veiculos/" + veiculoId)
                        .header("X-Internal-Token", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadEdicao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cor").value("Preto"));

        String payloadVenda = """
                { "veiculoId": "%s", "cpfComprador": "111.444.777-35", "dataVenda": "2026-09-12" }
                """.formatted(veiculoId);

        mockMvc.perform(post("/vendas").contentType(MediaType.APPLICATION_JSON).content(payloadVenda))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/vendas").contentType(MediaType.APPLICATION_JSON).content(payloadVenda))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornarBadRequestParaCpfInvalido() throws Exception {
        UUID veiculoId = UUID.randomUUID();
        String payloadCriacao = """
                { "id": "%s", "marca": "Fiat", "modelo": "Mobi", "ano": 2021, "cor": "Azul", "preco": 55000.00, "estadoConservacao": "USADO" }
                """.formatted(veiculoId);
        mockMvc.perform(post("/interno/veiculos")
                        .header("X-Internal-Token", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadCriacao))
                .andExpect(status().isCreated());

        String payloadVenda = """
                { "veiculoId": "%s", "cpfComprador": "111.111.111-11", "dataVenda": "2026-09-12" }
                """.formatted(veiculoId);

        mockMvc.perform(post("/vendas").contentType(MediaType.APPLICATION_JSON).content(payloadVenda))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornarNotFoundParaWebhookComCodigoInexistente() throws Exception {
        String payload = """
                { "codigoPagamento": "%s", "status": "APROVADO" }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/pagamentos/webhook").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isNotFound());
    }
}
