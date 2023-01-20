package io.github.robertograham.departuretests

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.Geolocation
import io.github.robertograham.departuretests.page.BusStopPage
import io.github.robertograham.departuretests.page.Index
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.utility.MountableFile
import spock.lang.Specification

import static org.testcontainers.containers.wait.strategy.Wait.forHttp
import static org.testcontainers.containers.wait.strategy.Wait.forLogMessage

final class Tests extends Specification {

    private static final def NETWORK = Network.newNetwork()

    private static final GenericContainer TRANSPORT_API = new GenericContainer('wiremock/wiremock:2.32.0-alpine')
            .withExposedPorts(8080)
            .withNetwork(NETWORK)
            .withCopyFileToContainer(MountableFile.forClasspathResource('wiremock/mappings'), '/home/wiremock/mappings')
            .tap { start() }

    private static final GenericContainer DEPARTURE_API = new GenericContainer('ghcr.io/robertograham/departure-api:latest')
            .waitingFor(forLogMessage('.*Started DepartureApiApplication.*', 1))
            .withExposedPorts(8080)
            .withNetwork(NETWORK)
            .withEnv([TRANSPORT_API_CLIENT_URL            : "http://${TRANSPORT_API.networkAliases.first()}:${TRANSPORT_API.exposedPorts.first()}" as String,
                      TRANSPORT_API_CLIENT_APPLICATION_ID : 'transportApiApplicationId',
                      TRANSPORT_API_CLIENT_APPLICATION_KEY: 'transportApiApplicationKey'])
            .tap { start() }

    private static final GenericContainer DEPARTURE_APP = new GenericContainer('ghcr.io/robertograham/departure-app:latest')
            .waitingFor(forHttp('/'))
            .withExposedPorts(80)
            .withNetwork(NETWORK)
            .withEnv('DEPARTURE_API_URL', "http://${DEPARTURE_API.networkAliases.first()}:${DEPARTURE_API.exposedPorts.first()}" as String)
            .tap { start() }

    private static final String BASE_URL = "http://$DEPARTURE_APP.host:$DEPARTURE_APP.firstMappedPort"

    private static final def PLAYWRIGHT = Playwright.create()

    private static final def BROWSER = PLAYWRIGHT.chromium()
            .launch(new BrowserType.LaunchOptions(args: ["--unsafely-treat-insecure-origin-as-secure=$BASE_URL" as String]))

    private def browserContext = BROWSER.newContext(new Browser.NewContextOptions(baseURL: BASE_URL,
            permissions: ['geolocation'],
            geolocation: new Geolocation(1.0, 1.0),
            locale: 'en-GB',
            timezoneId: 'Europe/London'))

    private def page = browserContext.newPage()

    def cleanup() {
        browserContext.close()
    }

    def cleanupSpec() {
        PLAYWRIGHT.close()
    }

    def 'view departures from index page'() {
        given:
        final def index = new Index(page)

        when: 'browser navigates to index page'
        index.navigate()

        then: 'bus stop shown with the correct details'
        verifyAll index.getBusStops().first(), {
            id == '1800SG08021'
            name == 'Beechurst Road - S-bound'
            locality == 'Adswood'
        }

        when: 'bus stop page link is clicked'
        final def busStopPage = index.busStops.first().open()

        then: 'bus stop page shown with the correct details'
        verifyAll busStopPage, {
            name == 'Beechurst Road - S-bound'
            locality == 'Adswood'
            verifyAll lines.first(), {
                name == '328'
                operator == 'Stagecoach Greater Manchester'
                verifyAll departures.first(), {
                    time == '23:09:00'
                    destination == 'Stockport'
                }
            }
        }
    }

    def 'view departures directly'() {
        given:
        final def busStopPage = new BusStopPage(page)

        when:
        busStopPage.navigate('1800SG08021')

        then: 'bus stop page shown with the correct details'
        verifyAll busStopPage, {
            name == 'Beechurst Road - S-bound'
            locality == 'Adswood'
            verifyAll lines.first(), {
                name == '328'
                operator == 'Stagecoach Greater Manchester'
                verifyAll departures.first(), {
                    time == '23:09:00'
                    destination == 'Stockport'
                }
            }
        }
    }

    def 'bus stop not found'() {
        given:
        final def busStopPage = new BusStopPage(page)

        when:
        busStopPage.navigate('notFound')

        then: 'bus stop page shown with the correct details'
        verifyAll busStopPage, {
            name == 'No bus stop found with id: notFound'
            !locality
            lines.empty
        }
    }
}
