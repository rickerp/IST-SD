# Guião de Demonstração

## 1. Preparação do Sistema

Para testar a aplicação e todos os seus componentes, é necessário preparar um ambiente com dados para proceder à verificação dos testes.

### 1.1. Compilar o Projeto

Primeiramente, é necessário instalar as dependências necessárias para o _silo_ e os clientes (_eye_ e _spotter_) e compilar estes componentes.
Para isso, basta ir à diretoria _root_ do projeto e correr o seguinte comando:

```
$ mvn clean install -DskipTests
```

Com este comando já é possível analisar se o projeto compila na íntegra.

### 1.2. _Silo_

Para proceder aos testes, é preciso o servidor _silo_ estar a correr.
Para isso basta ir à diretoria _silo-server_ e executar:

```
$ mvn exec:java
```

Este comando vai colocar o _silo_ no endereço _localhost_ e na porta _8081_. Visto que a default instance do silo é a 1, logo corre no porto 8081.

### 1.3. _Eye_

Vamos registar 3 câmeras e as respetivas observações.
Cada câmera vai ter o seu ficheiro de entrada próprio com observações já definidas.
Para isso basta ir à diretoria _eye_ e correr os seguintes comandos:

```
$ eye localhost 2181 Tagus 38.737613 -9.303164 < ../demo/input01.txt
$ eye localhost 2181 Alameda 30.303164 -10.737613 < ../demo/input03.txt
$ eye localhost 2181 Lisboa 32.737613 -15.303164 < ../demo/input04.txt
```

**Nota:** Para correr o script _eye_ é necessário fazer `mvn install` e adicionar ao _PATH_ ou utilizar diretamente os executáveis gerados na diretoria `target/appassembler/bin/`.

Depois de executar os comandos acima já temos o que é necessário para testar o sistema.

## 2. Teste das Operações

Nesta secção vamos correr os comandos necessários para testar todas as operações.
Cada subsecção é respetiva a cada operação presente no _silo_.

### 2.1. _cam_join_

Esta operação já foi testada na preparação do ambiente, no entanto ainda é necessário testar algumas restrições.

2.1.1. Teste das câmeras com nome duplicado e coordenadas diferentes.  
O servidor deve rejeitar esta operação.
Para isso basta executar um _eye_ com o seguinte comando:

```
$ eye localhost 2181 Tagus 10.0 10.0
```

2.1.2. Teste do tamanho do nome.  
O servidor deve rejeitar esta operação.
Para isso basta executar um _eye_ com o seguinte comando:

```
$ eye localhost 2181 ab 10.0 10.0
$ eye localhost 2181 abcdefghijklmnop 10.0 10.0
```

### 2.2. _report_

Esta operação já foi testada acima na preparação do ambiente.

No entanto falta testar o sucesso do comando _zzz_.
Na preparação foi adicionada informação que permite testar este comando.
Para testar basta abrir um cliente _spotter_ e correr o comando seguinte:

```
> trail person 123
```

O resultado desta operação deve ser 6 observações pelas câmeras _Tagus_, _Alameda_ e _Lisboa_ sendo que as observações realizadas pela câmara da _Alameda_ são espaçadas de cerca de 5 segundos.

### 2.3. _track_

Esta operação vai ser testada utilizando o comando _spot_ com um identificador.

2.3.1. Teste com uma pessoa inexistente no sistema (deve devolver vazio):

```
> spot person 1010101010
```

2.3.2. Teste com uma pessoa:

```
> spot person 123
person,123,<timestamp>,Lisboa,32.737613,-15.303164
```

2.3.3. Teste com um carro:

```
> spot car AABB77
car,AABB77,<timestamp>,Tagus,38.737613,-9.303164
```

### 2.4. _trackMatch_

Esta operação vai ser testada utilizando o comando _spot_ com um fragmento de identificador.

2.4.1. Teste com uma pessoa (deve devolver vazio):

```
> spot person 101010*
```

2.4.2. Testes com uma pessoa:

```
> spot person 12*
person,123,<timestamp>,Lisboa,32.737613,-15.303164

> spot person *71
person,171,<timestamp>,Tagus,38.737613,-9.303164

> spot person 1*9
person,189,<timestamp>,Lisboa,32.737613,-15.303164
```

2.4.3. Testes com duas ou mais pessoas:

```
> spot person 1*
person,123,<timestamp>,Lisboa,32.737613,-15.303164
person,171,<timestamp>,Tagus,38.737613,-9.303164
person,189,<timestamp>,Lisboa,32.737613,-15.303164
```

2.4.4. Testes com um carro:

```
> spot car AAB*
car,AABB77,<timestamp>,Tagus,38.737613,-9.303164

> spot car *ABB77
car,AABB77,<timestamp>,Tagus,38.737613,-9.303164

> spot car AA*77
car,AABB77,<timestamp>,Tagus,38.737613,-9.303164
```

2.4.5. Testes com dois ou mais carros:

```
> spot car *77
car,AABB77,<timestamp>,Tagus,38.737613,-9.303164
car,BBBB77,<timestamp>,Tagus,38.737613,-9.303164

> spot car *
car,AABB77,<timestamp>,Tagus,38.737613,-9.303164
car,BBBB77,<timestamp>,Tagus,38.737613,-9.303164
```

### 2.5. _trace_

Esta operação vai ser testada utilizando o comando _trail_ com um identificador.

2.5.1. Teste com uma pessoa (deve devolver vazio):

```
> trail person 10101010
```

2.5.2. Teste com uma pessoa:

```
> trail person 123
person,123,<timestamp>,Lisboa,32.737613 -15.303164
person,123,<timestamp>,Lisboa,32.737613 -15.303164
person,123,<timestamp>,Alameda,30.303164,-10.737613
person,123,<timestamp>,Alameda,30.303164,-10.737613
person,123,<timestamp>,Alameda,30.303164 -10.737613
person,123,<timestamp>,Tagus,38.737613,-9.303164

```

2.5.3. Teste com um carro (deve devolver vazio):

```
> trail car 12XD34
```

2.5.4. Teste com um carro:

```
> trail car AABB77
car,AABB77,<timestamp>,Tagus,38.737613,-9.303164
car,AABB77,<timestamp>,Tagus,38.737613,-9.303164
```

# Guião de Demonstração (multi réplica)

## 1. Preparação do Sistema

Para testar a aplicação e todos os seus componentes, é necessário preparar um ambiente com dados para proceder à verificação dos testes.

### 1.1. Compilar o Projeto

Primeiramente, é necessário instalar as dependências necessárias para o _silo_ e os clientes (_eye_ e _spotter_) e compilar estes componentes.
Para isso, basta ir à diretoria _root_ do projeto e correr o seguinte comando:

```
$ mvn clean install -DskipTests
```

Com este comando já é possível analisar se o projeto compila na íntegra.

### 1.2. _Silo_

Para proceder aos testes, é preciso lançar execuções dos servidores _silo_.
Para isso basta ir à diretoria _silo-server_ e executar:

```
$ mvn exec:java -Dinstance=<instance>
```

Este comando vai colocar o _silo_ no endereço _localhost_ e na porta _808\$(instance)_.
Vamos então criar 2 réplicas de modo a realizar os testes. Para tal deve correr os seguintes comandos em 2 terminais distintos:

```
$ mvn exec:java -Dinstance=1
```

```
$ mvn exec:java -Dinstance=2
```

### 1.3. _Eye_

Vamos registar 1 câmara e as respetivas observações.
Para isso basta ir à diretoria _eye_ e correr o seguinte comando:

```
$ eye localhost 2181 Tagus 38.737613 -9.303164 1 < ../demo/input01.txt
```

**Nota:** Para correr o script _eye_ é necessário fazer `mvn install` e adicionar ao _PATH_ ou utilizar diretamente os executáveis gerados na diretoria `target/appassembler/bin/`.

Depois de executar o comando acima poderá verificar no terminal da instância 1 do servidor que foram executados 3 uptades. O primeiro relativo ao cam_join do eye e os outros 2 a 2 conjuntos de observações.
Passados cerca de 30 segundos observará que o terminal da réplica 2 informa a chegada de 3 uptades através de mensagens gossip.

### 1.4. _Spotter_

Vamos abrir um spotter na réplica 2.
Para isso basta ir à diretoria _spotter_ e correr o seguinte comando:

```
$ spotter localhost 2181 2
```

Agora ao correr os seguintes comandos:

```
> spot person 123
person,123,<timestamp>,Tagus,38.737613,-9.303164
> spot car AABB77
car,AABB77,<timestamp>,Tagus,38.737613,-9.303164
```

Podemos observar que o eye registou a suas observações na réplica 1, e através de mensagens gossip a réplica 1 propagou esta informação até à réplica 2, como pode ser observado através dos comandos do spotter.

Agora para verificar o funcionamento da cache do cliente vamos terminar o servidor 2. Para isso basta ir até ao terminal do mesmo e premir enter ou CTRL-C. Em seguida volta-se a executar o comando:

```
$ mvn exec:java -Dinstance=2
```

De modo a gerar uma nova execução do servidor mas esta perdeu toda a informação que tinha anteriormente o que nos vais permitir observar o funcionamento da cache.
Para tal vamos correr no spotter os seguintes comandos( sem que sejam enviadas mensagens gossip com uptades, caso tal aconteca volte a terminar o servidor e correr os comandos):

```
> spot person 171

> spot person 123
person,123,<timestamp>,Tagus,38.737613,-9.303164
> spot car BBBB77

> spot car AABB77
car,AABB77,<timestamp>,Tagus,38.737613,-9.303164
```

Podemos observar que os comandos 1 e 3 como não foram executados antes de o servidor ser terminado o seu resultado não foi armazenado na cache. Mas que os comandos 2 e 4 foram executados e armazenada a sua informação.

Se agora esperar até haver a troca das mensagens gossip, o servidor 2 vai recuperar a informação que tinha perdido e ao executar de novo os comandos no spotter observamos que todos vão receber resposta.

### 2. _Troca de réplica_

Vamos agora mostar um cliente a trocar de réplica quando a réplica à qual este se encontrava ligada crasha. Para tal não pode ser especificada uma réplica na criação do cliente.

Primeiro crie 2 réplicas, executando os seguintes comandos na diretoria _silo-server_ :

```
$ mvn exec:java -Dinstance=1
```

```
$ mvn exec:java -Dinstance=2
```

Em seguida crie um eye sem especificar a réplica ao qual ele se liga:

```
$ eye localhost 2181 Tagus 38.737613 -9.303164
```

Poderá observar num dos terminais a execução de um update(Cam_join), esta réplica foi escolhida aleatoriamente entre as réplicas disponiveis.

Execute alguns comandos de report, por exemplo:

```
person,123

person,1234

person,123

person,123


```

Aguarde até que as gossip messages cheguem ao outro terminal (no terminal aparecerá uma mensagem: Received uptade{UUID} from gossip).

Agora termine a execução do terminal referente á réplica a que o eye se conectou, este terminal
tem mensagens do tipo Received uptade{UUID}. Repare que não são iguais ás mensagens presentes no outro terminal pois estas são do tipo Received uptade{UUID} from gossip.

Para terminar a execução da réplica basta premir enter ou CTRL-C no terminal com as mensagens Received uptade{UUID}.

Agora no eye realize os seguintes comandos:

```
person,123

person,1234


```

Poderá observar que estes comandos estão a ser imediatamente processados pela réplica à qual o eye se conseguiu reconectar. Esta reconexão foi feita escolhendo aleatoriamente uma réplica disponível após verificar que a réplica à qual o eye se encontrava previamente ligado crashou. Sendo que só existia uma réplica disponível a reconexão foi feita com a réplica que não foi terminada.
