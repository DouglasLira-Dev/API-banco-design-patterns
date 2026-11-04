package com.apibanco;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe de entrada (bootstrap) da aplicação <b>API-banco-design-patterns</b>.
 *
 * <p>Responsabilidade única: inicializar o contexto Spring Boot e subir o
 * servidor embutido (Tomcat). <b>Nenhuma</b> lógica de negócio deve viver
 * aqui — todo comportamento fica nos módulos de domínio, serviço,
 * repositório e controller, varridos automaticamente a partir deste
 * pacote-base {@code com.apibanco}.</p>
 *
 * <p>Anotada com {@link SpringBootApplication}, que é um atalho para:</p>
 * <ul>
 *   <li>{@code @Configuration}      — permite declarar beans;</li>
 *   <li>{@code @EnableAutoConfiguration} — ativa autoconfiguração do Spring Boot
 *       (datasource, JPA, web, etc.);</li>
 *   <li>{@code @ComponentScan}      — varre {@code com.apibanco.**} em busca de
 *       {@code @Component}, {@code @Service}, {@code @Repository}, {@code @Controller}.</li>
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