package io.github.robertograham.departuretests

import geb.spock.GebSpec
import io.github.robertograham.departuretests.page.BusStops
import org.mockserver.client.MockServerClient
import org.mockserver.model.JsonBody
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MockServerContainer
import org.testcontainers.containers.Network
import org.testcontainers.spock.Testcontainers
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
    private MockServerContainer mockServerContainer = new MockServerContainer()
            .withNetworkAliases('transport-api')
            .withNetwork(network)

    @Shared
    private GenericContainer departureApiContainer = new GenericContainer('docker.pkg.github.com/robertograham/departure-api/departure-api')
            .waitingFor(forLogMessage('.*Started DepartureApiApplication.*', 1))
            .withNetwork(network)
            .withNetworkAliases('departure-api')
            .withExposedPorts(8080)
            .withEnv('TRANSPORT_API_CLIENT_URL', "http://${mockServerContainer.networkAliases[1]}:${mockServerContainer.exposedPorts.first()}")

    @Shared
    private GenericContainer departureAppContainer = new GenericContainer('docker.pkg.github.com/robertograham/departure-app/departure-app')
            .waitingFor(forHttp('/'))
            .withNetwork(network)
            .withNetworkAliases(config.rawConfig.departureAppNetworkAlias as String)
            .withExposedPorts(80)
            .withEnv('DEPARTURE_API_URL', "http://${departureApiContainer.networkAliases[1]}:${departureApiContainer.exposedPorts.first()}")

    def setup() {
        browser.setBaseUrl("http://${departureAppContainer.networkAliases[1]}:${departureAppContainer.exposedPorts.first()}/")
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
        final def busStops = to BusStops

        expect:
        busStops.title == 'Departure App'
    }
}
