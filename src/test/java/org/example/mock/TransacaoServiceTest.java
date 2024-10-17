package org.example.mock;

import org.example.spy.BancoDeDados;
import org.example.spy.Conta;
import org.example.spy.Transacao;
import org.example.spy.TransacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TransacaoServiceTest {

    private org.example.spy.TransacaoService transacaoService;
    private BancoDeDados bancoDeDados;
    private Conta conta;

    @BeforeEach
    void setUp() {
        bancoDeDados = mock(BancoDeDados.class); // Mock do banco de dados
        transacaoService = new TransacaoService(bancoDeDados); // Passando o mock para a classe de serviço
        conta = spy(new Conta("12345")); // Criando um spy da conta
    }

    @Test
    void realizarSaque_pagamentoNaoAutorizado_returnsFalse() {
        // Arrange
        AutorizacaoPagamentoService pagamentoService = Mockito.mock(AutorizacaoPagamentoService.class);
        when(pagamentoService.autorizarPagamento(anyDouble())).thenReturn(false);

        transacaoService = new TransacaoService(pagamentoService);

        Conta conta = new Conta("1");
        conta.depositar(10);

        // Act
        boolean saqueRealizado = transacaoService.realizarSaque(conta, 9);

        //Assert
        assertFalse(saqueRealizado);
    }

    @Test
    void testRealizarDeposito() {
        double valor = 50.0;

        transacaoService.realizarDeposito(conta, valor);

        verify(conta).depositar(valor);

        ArgumentCaptor<Transacao> captor = ArgumentCaptor.forClass(Transacao.class);
        verify(bancoDeDados).salvarTransacao(eq(conta.getNumero()), captor.capture());

        Transacao transacao = captor.getValue();
        assertEquals(valor, transacao.getValor());
        assertEquals("DEPOSITO", transacao.getTipo());
    }

    @Test
    void testRealizarSaqueComSaldoSuficiente() {
        double valor = 150.0;

        transacaoService.realizarSaque(conta, valor);

        verify(conta).sacar(valor);

        ArgumentCaptor<Transacao> captor = ArgumentCaptor.forClass(Transacao.class);
        verify(bancoDeDados).salvarTransacao(eq(conta.getNumero()), captor.capture());

        Transacao transacao = captor.getValue();
        assertEquals(valor, transacao.getValor());
        assertEquals("SAQUE", transacao.getTipo());
    }

    @Test
    void testRealizarSaqueComSaldoInsuficiente() {
        double valor = 150.0;

        transacaoService.realizarSaque(conta, valor);

        verify(conta, never()).sacar(anyDouble());
    }
}