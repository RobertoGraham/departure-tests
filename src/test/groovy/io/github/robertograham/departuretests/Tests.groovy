package io.github.robertograham.departuretests

import geb.spock.GebSpec
import io.github.robertograham.departuretests.page.BusStops
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared

@Testcontainers
final class Tests extends GebSpec {

    @Shared
    private def departureAppNetworkAlias = config.rawConfig.departureAppNetworkAlias as String

    @Shared
    private GenericContainer departureAppContainer = new GenericContainer('docker.pkg.github.com/robertograham/departure-app/departure-app')
            .waitingFor(Wait.forHttp('/'))
            .withNetwork(config.rawConfig.network as Network)
            .withNetworkAliases(departureAppNetworkAlias)
            .withExposedPorts(80)
            .withEnv('DEPARTURE_API_URL', 'http://host.docker.internal:8080')

    def setup() {
        browser.setBaseUrl "http://$departureAppNetworkAlias:${departureAppContainer.exposedPorts[0]}/"
    }

    def 'test'() {
        given:
        final def busStops = to BusStops

        expect:
        busStops.title == 'Departure App'
    }
}
