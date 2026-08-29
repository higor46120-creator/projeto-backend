package br.edu.fiec.helptec.features.chamado;

public enum StatusChamado {
    AGUARDANDO_APROVACAO,  // recém-aberto, esperando o aprovador do solicitante
    APROVADO,              // aprovado, aguardando triagem do gerente
    REPROVADO,             // aprovador recusou o chamado
    EM_ATENDIMENTO,        // triado e alocado para um usuário de suporte
    RESOLVIDO,             // suporte concluiu o atendimento
    CANCELADO
}
