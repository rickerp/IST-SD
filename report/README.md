# Relatório do projeto Sauron

Sistemas Distribuídos 2019-2020, segundo semestre


## Autores

**Grupo T19**


| Número | Nome              | Utilizador                       | Correio eletrónico                  |
| -------|-------------------|----------------------------------| ------------------------------------|
| 90699  | Afonso Matos      | [afonsomatos](https://github.com/afonsomatos) | [afonsolfmatos@gmail.com](mailto:afonsolfmatos@gmail.com)   |
| 90741  | João Tomás Lopes  | [tomlopes](https://github.com/tomlopes)     | [joaotomaslopes@hotmail.com](mailto:joaotomaslopes@hotmail.com)     |
| 90775  | Ricardo Fernandes | [rickerp](https://github.com/rickerp) | [ricardo.s.fernandes@tecnico.ulisboa.pt](mailto:ricardo.s.fernandes@tecnico.ulisboa.pt) |

<img src="https://avatars0.githubusercontent.com/u/10373500?s=460&u=d55b8ec9104eaf2eac56d74f602580fe90ecfb29&v=4" height="150px" /> <img src="https://avatars1.githubusercontent.com/u/33103241?s=460&u=db5a1233e3f142ba48fd94532cfbf504ef14a13e&v=4" height="150px" /> <img src="https://avatars1.githubusercontent.com/u/32230933?s=460&u=d50670ea007c13559cbe4cd18aba7115436df700&v=4" height="150px" />


## Melhorias da primeira parte

- [spot * ordering by id](https://github.com/tecnico-distsys/T19-Sauron/commit/2f55891deda112f8bbbeb74b4f51093a24e17d21#diff-781a33c089feb1b4b74da871c8f53447L167-R170)
- [error handling (error mapping for gRPC)](https://github.com/tecnico-distsys/T19-Sauron/commit/282d6c1b22ea22548639189b3c583c04cf4c8f9b)


## Modelo de faltas

#### Faltas toleradas 

* Servidor crasha, enviando anteriormente uma mensagem gossip
* Estando uma cliente conectada a um servidor, apos esse mesmo crashar a cliente liga-se a outro servidor disponivel (caso nenhum servidor seja especificado no inicio)
* Se especificado o servidor no cliente, e esse mesmo crashar e voltar, ele reconecta-se. Caso o servidor mude de enedereco durante a execucao o cliente tambem se reconecta
* Caso o servidor retorne uma resposta destatualizada a uma query, o cliente usufrui de uma cache
* Através das gossip messages, se um servidor crashar recupera todos os updates das outras replicas

#### Faltas não toleradas

* O servidor crasha não enviando anteriormente uma mensagem gossip

## Solução

_(Figura da solução de tolerância a faltas)_

_(Breve explicação da solução, suportada pela figura anterior)_


## Protocolo de replicação

O protocolo usado é baseado na _gossip architecture_ (ver secção 18.4.1 do livro) com algumas alterações, nomeadamente a remoção do `Update log`, 

Em cada réplica, a cada ***x*** segundos (***x*** configurável), será enviada uma mensagem **gossip** para todas as outras réplicas encontradas atavés do **zkNaming**. Estas mensagens incluem a número da réplica emissora, um **log** com os updates mais recentes para cada réplica, não incluindo updates que estas já tenham, e um **timestamp** que refere o ultimo estado conhecido de cada réplica, por parte da réplica emissora.
Ao receber uma *gossip message*, a réplica adiciona os updates recebidos se esta já não os tiver. Atualiza o seu timestamp se houve updates válidos.


## Opções de implementação

_(Descrição de opções de implementação, incluindo otimizações e melhorias introduzidas)_



## Notas finais

_(Algo mais a dizer?)_
