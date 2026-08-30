# Especificação de Requisitos Não Funcionais

***Sistema de Empréstimo de Projetores — CCT/Unifor***

## Histórico de Versões

<!-- markdownlint-disable MD060 -->

| Data       | Versão | Descrição                                      | Autor                  |
| ---------- | ------- | ---------------------------------------------- | ---------------------- |
| 20/05/2026 | 1.0     | Criação inicial do documento de RNF            | Equipe do Projeto      |
|            |         |                                                |                        |
|            |         |                                                |                        |
|            |         |                                                |                        |

<!-- markdownlint-enable MD060 -->

## 1. Requisitos de Produto

Especificam o comportamento da solução.

### 1.1. Eficiência de Desempenho

Capacidade da solução operar no tempo, rendimento e recursos definidos.

#### 1.1.1. Comportamento temporal

O sistema deve identificar o professor a partir da leitura biométrica e retornar os dados correspondentes do Sistema Acadêmico em no máximo **3 segundos** após a captura da digital, considerando uma conexão de rede local padrão da instituição. O preenchimento automático do formulário virtual deve ocorrer em menos de **1 segundo** após a identificação biométrica bem-sucedida.

#### 1.1.2. Capacidade

O sistema deve suportar simultaneamente ao menos **50 funcionários** acessando o sistema em paralelo, sem degradação de desempenho. O banco de dados deve comportar o registro histórico de pelo menos **10 anos** de empréstimos de projetores sem impacto no tempo de resposta das consultas.

#### 1.1.3. Uso de recursos

O sistema deve operar nos computadores institucionais já disponíveis no CCT. A leitora biométrica deve ser o único hardware adicional necessário, conectando-se via USB ou rede local sem instalação de drivers especializados nos computadores. O consumo de memória RAM da aplicação não deve ultrapassar **512 MB** em operação normal.

---

### 1.2. Flexibilidade (portabilidade)

Capacidade da solução adaptar-se a mudanças em seus requisitos, contextos de uso e ambientes.

#### 1.2.1. Escalabilidade

O sistema deve suportar o cadastro de novos projetores, funcionários e professores sem necessidade de alteração na arquitetura da solução. A solução deve permitir expansão para outros departamentos da Unifor que também realizem empréstimo de equipamentos, com esforço mínimo de configuração.

#### 1.2.2. Adaptabilidade

O sistema deve ser acessível via navegadores web modernos (Chrome, Firefox, Edge — versões lançadas nos últimos 2 anos), sem necessidade de instalação de software adicional nos computadores dos funcionários.

#### 1.2.3. Instalabilidade

O sistema deve ser implantável no ambiente de servidores da Unifor em até **1 dia útil**, com documentação de instalação e configuração disponível. Deve ser possível realizar atualizações sem interrupção do serviço por mais de **15 minutos**.

#### 1.2.4. Substituibilidade

O sistema deve ser capaz de substituir integralmente o processo manual atual (formulários em papel e chave física) sem perda de informação histórica, desde que os dados existentes sejam migrados para o novo formato.

---

### 1.3. Confiabilidade

Capacidade da solução de manter o desempenho sob condições específicas.

#### 1.3.1. Maturidade

O sistema deve apresentar taxa de falhas inferior a **1 ocorrência por mês** em condições normais de operação, após o período inicial de estabilização de 30 dias.

#### 1.3.2. Disponibilidade

O sistema deve estar disponível durante todo o horário de funcionamento do CCT, com disponibilidade mínima de **99%** no período letivo (segunda a sábado, das 07h às 22h). Manutenções programadas devem ocorrer fora do horário de pico de empréstimos.

#### 1.3.3. Tolerância a falhas

Em caso de falha momentânea na conexão com o banco de dados, o sistema deve exibir uma mensagem de erro clara ao funcionário em menos de **5 segundos**, sem travar a interface. Nenhuma operação de registro deve ser perdida silenciosamente; o sistema deve alertar o funcionário em caso de falha no salvamento.

#### 1.3.4. Recuperabilidade

O sistema deve realizar backups automáticos dos dados a cada **24 horas**. Em caso de falha crítica, deve ser possível restaurar o sistema ao último estado consistente em no máximo **4 horas**.

---

### 1.4. Segurança

Capacidade da solução para proteger as informações e garantir operações seguras.

#### 1.4.1. Confidencialidade

O acesso ao sistema deve ser restrito a usuários autorizados — funcionários do setor de empréstimos e coordenador do CCT — mediante autenticação com credenciais institucionais. O coordenador possui perfil de acesso adicional que habilita a consulta ao módulo de relatórios gerenciais. Os dados dos professores (nome, matrícula, disciplinas) não devem ser visíveis a usuários não autenticados.

#### 1.4.2. Integridade

Registros de empréstimo, devolução e histórico não devem poder ser alterados ou excluídos por usuários comuns. Toda modificação em registros deve ser rastreada com identificação do responsável e carimbo de data e hora.

#### 1.4.3. Não repúdio

O sistema deve registrar de forma não alterável cada empréstimo e devolução, associando a operação ao funcionário que a realizou e ao professor beneficiário, com data e hora precisas, garantindo evidência auditável das ações.

#### 1.4.4. Autenticidade (autenticação)

O sistema deve autenticar os funcionários via login integrado ao sistema de identidade da Unifor (ex.: LDAP ou SSO institucional), impedindo o acesso com credenciais inválidas ou expiradas.

#### 1.4.5. Resistência

O sistema deve ser protegido contra tentativas de acesso não autorizado, aplicando bloqueio temporário de conta após **5 tentativas consecutivas** de login com credenciais incorretas. A comunicação entre cliente e servidor deve ser cifrada via HTTPS.

---

### 1.5. Privacidade

#### 1.5.1. Licitude

O tratamento de dados pessoais de professores (nome, matrícula, disciplinas lecionadas, histórico de empréstimos) deve estar amparado pela Lei Geral de Proteção de Dados (LGPD — Lei nº 13.709/2018) e pelas políticas internas da Unifor.

#### 1.5.2. Finalidade

Os dados pessoais coletados têm finalidade exclusiva de operacionalizar o empréstimo de projetores e gerar relatórios gerenciais internos do CCT. Nenhum dado será utilizado para finalidades incompatíveis com o empréstimo de equipamentos.

#### 1.5.3. Necessidade

Serão coletados apenas os dados estritamente necessários para identificar o professor, registrar o empréstimo e gerar os relatórios definidos: nome, matrícula, disciplina(s) e horário da solicitação. Dados sensíveis não relacionados ao empréstimo não devem ser coletados.

#### 1.5.4. Tratamento

Os dados pessoais percorrem o seguinte ciclo: coleta (pelo funcionário no ato do empréstimo) → armazenamento (banco de dados institucional) → uso (geração de relatórios internos) → retenção (mínimo de 5 anos para fins de auditoria) → eliminação (conforme política de retenção da Unifor). Nenhum dado será compartilhado com terceiros sem amparo legal.

---

### 1.6. Capacidade de Interação (UX, usabilidade e acessibilidade)

Capacidade da solução relacionar-se com os usuários em variados contextos de uso.

#### 1.6.1. Reconhecimento de adequação

Ao acessar o sistema, o funcionário deve compreender imediatamente que a ferramenta destina-se ao gerenciamento de empréstimos de projetores, sem necessidade de leitura de manual.

#### 1.6.2. Facilidade de aprendizado (learnability)

Um funcionário sem experiência prévia com o sistema deve ser capaz de realizar um empréstimo completo sem assistência após **no máximo 1 sessão de treinamento** de até 30 minutos.

#### 1.6.3. Operabilidade

O fluxo de registro de um empréstimo deve ser concluído em **no máximo 4 cliques ou interações** na interface, após a digitação do nome do professor. Campos obrigatórios devem ser claramente sinalizados.

#### 1.6.4. Proteção contra erros do usuário

O sistema deve validar a identidade do professor antes de preencher o formulário, exibindo uma tela de confirmação com os dados encontrados. Caso o nome pesquisado retorne mais de um resultado, o sistema deve apresentar lista de seleção para eliminar ambiguidades.

#### 1.6.5. Inclusividade (acessibilidade)

A interface deve seguir as diretrizes de acessibilidade WCAG 2.1 nível AA, garantindo uso por funcionários com deficiências visuais leves ou que utilizem tecnologias assistivas.

#### 1.6.6. Assistência ao usuário (acessibilidade)

O sistema deve exibir mensagens de orientação contextual em caso de erro (ex.: "Nenhum professor encontrado com esse nome. Tente usar apenas o sobrenome.") e disponibilizar uma seção de ajuda rápida acessível a partir de qualquer tela.

#### 1.6.7. Engajamento do usuário

O sistema deve fornecer feedback visual imediato para cada ação do funcionário (ex.: confirmação de empréstimo registrado, alerta de projetor indisponível), reduzindo incerteza durante o atendimento.

---

### 1.7. Manutenibilidade

Capacidade da solução de evoluir com custo baixo e sem introdução de novos erros.

#### 1.7.1. Modularidade

O sistema deve ser desenvolvido em módulos independentes (ex.: módulo de autenticação, módulo de empréstimos, módulo de relatórios), de modo que a alteração em um módulo não impacte os demais.

#### 1.7.2. Reusabilidade

Componentes de interface e regras de negócio (ex.: busca de professor, geração de formulário) devem ser reutilizáveis em eventuais expansões do sistema para outros tipos de equipamentos.

#### 1.7.3. Analisabilidade

O sistema deve gerar logs de operação com nível de detalhe suficiente para diagnosticar falhas em até **2 horas** sem necessidade de acesso ao código-fonte.

#### 1.7.4. Modificabilidade

A adição de um novo campo ao formulário de empréstimo ou de um novo tipo de relatório deve ser realizável por um desenvolvedor em no máximo **1 dia útil**, sem impacto nas funcionalidades existentes.

#### 1.7.5. Testabilidade

O sistema deve possuir cobertura mínima de **70% por testes automatizados** nas funcionalidades críticas (busca de professor, registro de empréstimo e devolução, geração de relatório).

---

### 1.8. Compatibilidade

Capacidade da solução trocar informações e coexistir com outras soluções.

#### 1.8.1. Interoperabilidade

O sistema deve integrar-se à base de dados acadêmica da Unifor para obter automaticamente as informações do professor (nome completo, matrícula, disciplinas lecionadas no semestre vigente) a partir do nome informado pelo funcionário.

#### 1.8.2. Coexistência

O sistema deve operar nos computadores institucionais do CCT sem conflito com outros softwares já instalados (ex.: sistemas acadêmicos, navegadores, suítes de escritório).

---

### 1.9. Segurança Operacional (safety)

Capacidade da solução ser segura em relação aos riscos operacionais.

#### 1.9.1. Restrição operacional

O sistema não deve permitir o registro de empréstimo de um projetor que já conste como emprestado e não devolvido, impedindo inconsistências no controle de estoque.

#### 1.9.2. Identificação de riscos

O sistema deve alertar o funcionário quando um professor possuir empréstimos em aberto (projetores não devolvidos) antes de autorizar um novo empréstimo.

#### 1.9.3. Segurança contra falhas (fail-safe)

Em caso de falha durante o registro de um empréstimo, o sistema não deve alterar o estado do projetor no banco de dados (operação atômica), garantindo consistência entre o estado real e o registrado.

#### 1.9.4. Aviso de perigo

O sistema deve notificar o administrador quando o estoque de projetores disponíveis atingir quantidade igual ou inferior a **20%** do total cadastrado, possibilitando ação preventiva.

#### 1.9.5. Integração segura

A integração com a base de dados acadêmica da Unifor deve ocorrer por meio de APIs autenticadas e com transmissão cifrada, sem exposição de credenciais de acesso no código da aplicação.

---

### 1.10. Adequação funcional

Capacidade da solução de fornecer funções que atendam às necessidades dos usuários.

#### 1.10.1. Completude funcional

O sistema deve cobrir todo o fluxo de empréstimo: identificação biométrica do professor, preenchimento automático do formulário virtual, registro do empréstimo, envio de email de confirmação ao professor, controle de devolução e geração de relatórios gerenciais para o coordenador do CCT (professores mais frequentes, disciplinas com maior demanda, horários de pico, tempo médio de empréstimo por equipamento).

#### 1.10.2. Corretude funcional

Os dados exibidos automaticamente pelo sistema (nome, matrícula, disciplinas) devem corresponder exatamente às informações registradas na base acadêmica da Unifor, sem transformações ou truncamentos indevidos.

#### 1.10.3. Adequação funcional

O sistema deve reduzir o tempo médio de atendimento por empréstimo de projetor para no máximo **3 minutos**, em comparação ao processo manual atual.

---

## 2. Requisitos Externos

Derivados de fatores externos à solução e ao seu processo de desenvolvimento.

### 2.1. Ético

O sistema não deve ser utilizado para monitoramento ou avaliação de desempenho individual dos professores. Os relatórios gerados devem ser usados exclusivamente para fins de gestão de recursos do CCT, e seu uso deve ser transparente para os envolvidos.

### 2.2. Regulatório

O sistema deve estar em conformidade com as normas internas da Unifor para sistemas de informação institucionais, incluindo padrões de segurança da informação e políticas de uso de dados acadêmicos definidas pela instituição.

### 2.3. Legislativo

O sistema deve estar em conformidade com:

- **LGPD** (Lei nº 13.709/2018) — proteção de dados pessoais dos professores;
- **Marco Civil da Internet** (Lei nº 12.965/2014) — uso responsável de dados em ambientes digitais;
- Normativas internas da Unifor sobre privacidade e segurança da informação.

---

## 3. Requisitos Organizacionais

Derivados de políticas e procedimentos do cliente e do fornecedor.

### 3.1. Ambientais

O sistema será executado nos computadores já disponíveis no setor de empréstimos do CCT/Unifor, conectados à rede interna da instituição. Não há previsão de acesso remoto externo à rede da Unifor para os funcionários do setor.

### 3.2. Operacionais

O sistema será operado pelos funcionários do setor de empréstimos e pelo coordenador do CCT durante o horário de funcionamento do departamento. Os professores **não** terão acesso direto ao sistema; sua única interação com o equipamento é a leitura biométrica na leitora física, mediada pelo funcionário. O coordenador acessa o sistema apenas para consultar relatórios gerenciais.

### 3.3. Desenvolvimento

O sistema deve ser desenvolvido utilizando tecnologias compatíveis com a infraestrutura atual da Unifor. A equipe de desenvolvimento deve seguir metodologia ágil com entregas incrementais, priorizando as funcionalidades de empréstimo e devolução na primeira versão. O código-fonte deve ser versionado em repositório institucional e documentado em português.

---

## 4. Checklist de Validação do Artefato (RNF)

Use este checklist antes de concluir a versão do documento.

### 4.1. Estrutura e escopo

* [ ] O documento possui histórico de versões preenchido.
* [ ] O escopo da solução está claro no documento.
* [ ] Há requisitos registrados nas seções aplicáveis (produto, externos e organizacionais).
* [ ] Requisitos não aplicáveis estão explicitamente marcados como "Não se aplica", quando necessário.

### 4.2. Qualidade dos requisitos

* [ ] Cada requisito está escrito de forma objetiva e verificável.
* [ ] Cada requisito possui critério mensurável (tempo, percentual, limite, condição ou evidência).
* [ ] Não há requisito ambíguo com termos vagos (ex.: "rápido", "seguro", "fácil") sem métrica.
* [ ] Requisitos duplicados ou conflitantes foram eliminados.

### 4.3. Conformidade e rastreabilidade

* [ ] Requisitos regulatórios/legais relevantes foram registrados.
* [ ] Requisitos de privacidade e segurança foram contemplados quando aplicáveis.
* [ ] Os requisitos estão alinhados com visão da demanda, glossário e casos de uso.
* [ ] Existe rastreabilidade dos requisitos para fontes de negócio, norma ou decisão técnica.

### 4.4. Prontidão para uso

* [ ] Os requisitos podem ser usados como base para implementação e testes.
* [ ] Há insumos suficientes para criar critérios de aceitação.
* [ ] O documento foi revisado por pares.
* [ ] A versão está pronta para aprovação/publicação.
