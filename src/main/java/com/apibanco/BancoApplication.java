package com.apibanco;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe de entrada (bootstrap) da aplicação <b>API-banco-design-patterns</b>.
 *
 * <p>Responsabilidade única: inicializar o contexto Spring Boot e subir o
 * servidor embutido (Tomcat). <b>Nenhuma</b> lógica de negócio deve viver
 * aqui &mdash; todo comportamento fica nos módulos de domínio, serviço,
 * repositório e controller, varridos automaticamente a partir deste
 * pacote-base {@code com.apibanco}.</p>
 *
 * <p>Anotada com {@link SpringBootApplication}, que é um atalho para:</p>
 * <ul>
 *   <li>{@code @Configuration} &mdash; permite declarar beans;</li>
 *   <li>{@code @EnableAutoConfiguration} &mdash; ativa autoconfiguração do
 *       Spring Boot (datasource, JPA, web, etc.);</li>
 *   <li>{@code @ComponentScan} &mdash; varre {@code com.apibanco.**} em busca
 *       de {@code @Component}, {@code @Service}, {@code @Repository},
 *       {@code @Controller}.</li>
 * </ul>
 *
 * <p><b>Como executar:</b></p>
 * <pre>{@code
 *   mvn spring-boot:run
 *   # ou
 *   java -jar target/api-banco-design-patterns-0.0.1-SNAPSHOT.jar
 * }</pre>
 *
 * @author  equipe API-banco
 * @since   0.0.1-SNAPSHOT
 * @see     org.springframework.boot.autoconfigure.SpringBootApplication
 */
@SpringBootApplication
public class BancoApplication {

    /**
     * Construtor público &mdash; <b>não</b> privado.
     *
     * <p>O Spring precisa instanciar a classe anotada com
     * {@code @SpringBootConfiguration} para gerar o proxy CGLIB que
     * processa as configurações. Um construtor {@code private} faz o
     * CGLIB falhar com
     * {@code IllegalArgumentException: No visible constructors} &mdash;
     * o que quebra <b>todos</b> os {@code @WebMvcTest} e
     * {@code @SpringBootTest}.</p>
     *
     * <p>Deixamos o construtor explícito (em vez de implícito) apenas
     * para documentar essa pegadinha &mdash; o comportamento é o mesmo
     * de não declarar construtor.</p>
     */
    public BancoApplication() {
        // construtor público obrigatório para o Spring/CGLIB
    }

    /**
     * Ponto de entrada do processo Java.
     *
     * <p>Delega ao {@link SpringApplication} a criação do contexto, a
     * varredura de componentes e a inicialização do servidor HTTP
     * (porta configurada em {@code application.yml}, padrão {@code 8080}).</p>
     *
     * @param args argumentos de linha de comando repassados ao Spring
     *             (ex.: {@code --server.port=9090}); podem ser {@code null}
     */
    public static void main(String[] args) {
        SpringApplication.run(BancoApplication.class, args);
    }
}