package io.github.robertograham.departuretests

import geb.spock.GebSpec
import io.github.robertograham.departuretests.page.BusStopPage
import io.github.robertograham.departuretests.page.Index
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.spock.Testcontainers
import org.testcontainers.utility.MountableFile
import spock.lang.Shared

import static org.testcontainers.containers.wait.strategy.Wait.forHttp
import static org.testcontainers.containers.wait.strategy.Wait.forLogMessage

@Testcontainers
final class Tests extends GebSpec {

    @Shared
    private def network = config.rawConfig.network as Network

    @Shared
    private GenericContainer wireMockContainer = new GenericContainer('wiremock/wiremock:2.32.0-alpine')
            .tap { networkAliases = ['transport-api'] }
            .withNetwork(network)
            .withExposedPorts(8080)
            .withCopyFileToContainer(MountableFile.forClasspathResource('wiremock/mappings'), '/home/wiremock/mappings')

    @Shared
    private GenericContainer departureApiContainer = new GenericContainer('ghcr.io/robertograham/departure-api:latest')
            .waitingFor(forLogMessage('.*Started DepartureApiApplication.*', 1))
            .tap { networkAliases = ['departure-api'] }
            .withNetwork(network)
            .withExposedPorts(8080)
            .withEnv([TRANSPORT_API_CLIENT_URL            : "http://${wireMockContainer.networkAliases.first()}:${wireMockContainer.exposedPorts.first()}" as String,
                      TRANSPORT_API_CLIENT_APPLICATION_ID : 'transportApiApplicationId',
                      TRANSPORT_API_CLIENT_APPLICATION_KEY: 'transportApiApplicationKey'])

    @Shared
    private GenericContainer departureAppContainer = new GenericContainer('ghcr.io/robertograham/departure-app:latest')
            .waitingFor(forHttp('/'))
            .tap { networkAliases = [config.rawConfig.departureAppNetworkAlias as String] }
            .withNetwork(network)
            .withExposedPorts(80)
            .withEnv('DEPARTURE_API_URL', "http://${departureApiContainer.networkAliases.first()}:${departureApiContainer.exposedPorts.first()}")

    def 'view departures from index page'() {
        when: 'browser navigates to index page'
        to Index

        then: 'bus stop shown with the correct details'
        verifyAll at(Index).busStops.first(), {
            id == '1800SG08021'
            name == 'Beechurst Road - S-bound'
            locality == 'Adswood'
        }

        when: 'bus stop page link is clicked'
        at(Index).busStops.first().busStopPageLink.click()

        then: 'bus stop page shown with the correct details'
        verifyAll at(BusStopPage), {
            name == 'Beechurst Road - S-bound'
            locality == 'Adswood'
            verifyAll lines.first(), {
                name == '328'
                operator == 'Stagecoach Greater Manchester'
                verifyAll departures.first(), {
                    time == '11:09:00 PM'
                    destination == 'Stockport'
                }
            }
        }
    }

    def 'view departures directly'() {
        expect: 'bus stop page shown with the correct details'
        verifyAll at(to(BusStopPage, '1800SG08021', 'departures')), {
            name == 'Beechurst Road - S-bound'
            locality == 'Adswood'
            verifyAll lines.first(), {
                name == '328'
                operator == 'Stagecoach Greater Manchester'
                verifyAll departures.first(), {
                    time == '11:09:00 PM'
                    destination == 'Stockport'
                }
            }
        }
    }

    def 'bus stop not found'() {
        expect: 'bus stop page shown with the correct details'
        verifyAll at(to(BusStopPage, 'notFound', 'departures')), {
            name == 'No bus stop found with id: notFound'
            !locality
            lines.empty
        }
    }
}
