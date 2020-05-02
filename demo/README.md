# Guião de Demonstração (exemplo)

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
$ eye localhost 2181 Tagus 38.737613 -9.303164 < input01.txt
$ eye localhost 2181 Alameda 30.303164 -10.737613 < input03.txt
$ eye localhost 2181 Lisboa 32.737613 -15.303164 < input04.txt
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
