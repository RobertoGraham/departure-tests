package io.github.robertograham.departuretests

import geb.spock.GebSpec
import io.github.robertograham.departuretests.page.Index
import org.mockserver.client.MockServerClient
import org.mockserver.model.JsonBody
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MockServerContainer
import org.testcontainers.containers.Network
import org.testcontainers.spock.Testcontainers
import org.testcontainers.utility.DockerImageName
import spock.lang.Shared

import java.time.ZonedDateTime

import static com.google.common.net.HttpHeaders.CONTENT_TYPE
import static com.google.common.net.MediaType.JSON_UTF_8
import static org.mockserver.model.HttpRequest.request
import static org.mockserver.model.HttpResponse.response
import static org.testcontainers.containers.wait.strategy.Wait.forHttp
import static org.testcontainers.containers.wait.strategy.Wait.forLogMessage

@Testcontainers
final class Tests extends GebSpec {

    @Shared
    private def network = config.rawConfig.network as Network

    @Shared
    private MockServerContainer mockServerContainer = new MockServerContainer(DockerImageName.parse('mockserver/mockserver')
            .withTag('mockserver-5.11.2'))
            .tap { networkAliases = ['transport-api'] }
            .withNetwork(network)

    @Shared
    private GenericContainer departureApiContainer = new GenericContainer('ghcr.io/robertograham/departure-api:latest')
            .waitingFor(forLogMessage('.*Started DepartureApiApplication.*', 1))
            .tap { networkAliases = ['departure-api'] }
            .withNetwork(network)
            .withExposedPorts(8080)
            .withEnv('TRANSPORT_API_CLIENT_URL', "http://${mockServerContainer.networkAliases.first()}:${mockServerContainer.exposedPorts.first()}")

    @Shared
    private GenericContainer departureAppContainer = new GenericContainer('ghcr.io/robertograham/departure-app:latest')
            .waitingFor(forHttp('/'))
            .tap { networkAliases = [config.rawConfig.departureAppNetworkAlias as String] }
            .withNetwork(network)
            .withExposedPorts(80)
            .withEnv('DEPARTURE_API_URL', "http://${departureApiContainer.networkAliases.first()}:${departureApiContainer.exposedPorts.first()}")

    def setup() {
        browser.setBaseUrl("http://${departureAppContainer.networkAliases.first()}:${departureAppContainer.exposedPorts.first()}/")
        new MockServerClient(mockServerContainer.host, mockServerContainer.serverPort)
                .when(request('/places.json').withQueryStringParameters(lat: ['[-+]?[0-9]*\\.?[0-9]+'],
                        lon: ['[-+]?[0-9]*\\.?[0-9]+'],
                        type: ['bus_stop'],
                        app_key: ['.*'],
                        app_id: ['.*']))
                .respond(response().withBody(JsonBody.json([request_time    : ZonedDateTime.now() as String,
                                                            source          : 'source',
                                                            acknowledgements: 'acknowledgements',
                                                            member          : [[type       : 'bus_stop',
                                                                                atcocode   : 'atcocode',
                                                                                name       : 'name',
                                                                                description: 'description',
                                                                                latitude   : 0,
                                                                                longitude  : 0,
                                                                                accuracy   : 0]]]))
                        .withHeader(CONTENT_TYPE, JSON_UTF_8 as String))
    }

    def 'test'() {
        given:
        final def index = to Index

        expect:
        at index, {
            busStops.first().id == 'atcocode'
        }
    }
}
