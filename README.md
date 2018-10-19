# X-Jus

X-Jus é um webservice de indexação e busca textual. 
Ele pode ser facilmente integrado a qualquer sistema e irá indexar registros e permitir que se realize buscas textuais.

Existem várias soluções open-source para buscas textuais, como por exemplo o [Apache SOLR](http://lucene.apache.org/solr/) 
ou o [ElasticSearch](https://www.elastic.co/). O X-jus é uma solução diferente, pois foi criado espeficamente para funcionar
na nuvem, usando tecnologia Google.

O X-Jus se comunica com os sistemas através de dois webservices, um para obter dados dos registros que precisam ser indexados e 
outro para receber consultas e retornar os resultados.

### Expondo seus dados para que o X-Jus faça a indexação

Para expor seus dados para o X-Jus é necessário abrir para a Internet um webservice REST com apenas 3 métodos:

* GET /all-references?max=20&lastid=0000000001
* GET /changed-references?max=20&lastdate=2018-01-01T00:00:00.000&lastid=0000000001
* GET /record/0000000001

O formato de retorno para cada um dos métodos acima, bem como documentação detalhada, podem ser visualizados 
[aqui](http://x-jus-trf2.appspot.com/mock/record/api/v1/swagger-ui). O arquivo 
swagger.yaml está [aqui](http://x-jus-trf2.appspot.com/mock/record/api/v1/swagger.yaml).

### Pesquisando no índice do X-Jus

Para realizar um busca no X-Jus, utilise o método:

* GET /index/nomedoindice/query?filter=pesquisa&page=0&perpage=10

O formato de retorno da busca, bem como documentação detalhada deste webservice e de webservices auxiliares, pode ser visualizada 
[aqui](http://x-jus-trf2.appspot.com/api/v1/swagger-ui). O arquivo 
swagger.yaml está [aqui](http://x-jus-trf2.appspot.com/api/v1/swagger.yaml).

O X-Jus é um projeto do TRF2 (www.trf2.jus.br).
