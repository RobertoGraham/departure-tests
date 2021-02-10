package io.github.robertograham.departuretests

import geb.spock.GebSpec
import io.github.robertograham.departuretests.page.BusStopPage
import io.github.robertograham.departuretests.page.Index
import org.mockserver.client.MockServerClient
import org.mockserver.model.JsonBody
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MockServerContainer
import org.testcontainers.containers.Network
import org.testcontainers.spock.Testcontainers
import org.testcontainers.utility.DockerImageName
import spock.lang.Shared

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
            .withEnv([TRANSPORT_API_CLIENT_URL            : "http://${mockServerContainer.networkAliases.first()}:${mockServerContainer.exposedPorts.first()}" as String,
                      TRANSPORT_API_CLIENT_APPLICATION_ID : 'transportApiApplicationId',
                      TRANSPORT_API_CLIENT_APPLICATION_KEY: 'transportApiApplicationKey'])

    @Shared
    private GenericContainer departureAppContainer = new GenericContainer('ghcr.io/robertograham/departure-app:latest')
            .waitingFor(forHttp('/'))
            .tap { networkAliases = [config.rawConfig.departureAppNetworkAlias as String] }
            .withNetwork(network)
            .withExposedPorts(80)
            .withEnv('DEPARTURE_API_URL', "http://${departureApiContainer.networkAliases.first()}:${departureApiContainer.exposedPorts.first()}")

    def setup() {
        final def mockServerClient = new MockServerClient(mockServerContainer.host, mockServerContainer.serverPort)
        mockServerClient.when(request('/places.json').withQueryStringParameters(lat: ['[-+]?[0-9]*\\.?[0-9]+'],
                lon: ['[-+]?[0-9]*\\.?[0-9]+'],
                type: ['bus_stop'],
                app_key: ['transportApiApplicationKey'],
                app_id: ['transportApiApplicationId']))
                .respond(response().withBody(JsonBody.json('''\
{
  "request_time": "2021-02-04T23:20:26+00:00",
  "source": "NaPTAN",
  "acknowledgements": "Contains DfT NaPTAN bus stops data",
  "member": [
    {
      "type": "bus_stop",
      "name": "Beechurst Road - S-bound",
      "latitude": 53.39014,
      "longitude": -2.18307,
      "accuracy": 20,
      "atcocode": "1800SG08021",
      "description": "Adswood",
      "distance": 0
    }
  ]
}'''))
                        .withHeader(CONTENT_TYPE, JSON_UTF_8 as String))
        mockServerClient.when(request('/bus/stop/1800SG08021/live.json').withQueryStringParameters(group: ['no'],
                limit: ['300'],
                nextbuses: ['no'],
                app_key: ['transportApiApplicationKey'],
                app_id: ['transportApiApplicationId']))
                .respond(response().withBody(JsonBody.json('''\
{
  "atcocode": "1800SG08021",
  "smscode": "MANJAWMP",
  "request_time": "2021-02-04T23:04:24+00:00",
  "name": "Beechurst Road",
  "stop_name": "Beechurst Road",
  "bearing": "S",
  "indicator": "opp",
  "locality": "Adswood",
  "location": {
    "type": "Point",
    "coordinates": [
      -2.18307,
      53.39014
    ]
  },
  "departures": {
    "all": [
      {
        "mode": "bus",
        "line": "328",
        "line_name": "328",
        "direction": "Stockport",
        "operator": "SCMN",
        "operator_name": "Stagecoach Greater Manchester",
        "date": "2021-02-04",
        "aimed_departure_time": "23:09",
        "expected_departure_date": null,
        "expected_departure_time": null,
        "best_departure_estimate": "23:09",
        "dir": "outbound",
        "id": "https://transportapi.com/v3/uk/bus/route/SCMN/328/outbound/1800SG08021/2021-02-04/23:09/timetable.json?app_id=transportApiApplicationId&app_key=transportApiApplicationKey",
        "source": "tnds timetable (nextbuses disabled)"
      }
    ]
  }
}'''))
                        .withHeader(CONTENT_TYPE, JSON_UTF_8 as String))
        mockServerClient.when(request('/places.json').withQueryStringParameters(query: ['1800SG08021'],
                type: ['bus_stop'],
                app_key: ['transportApiApplicationKey'],
                app_id: ['transportApiApplicationId']))
                .respond(response().withBody(JsonBody.json('''\
{
  "request_time": "2021-02-05T00:43:11+00:00",
  "source": "NaPTAN",
  "acknowledgements": "Contains DfT NaPTAN bus stops data",
  "member": [
    {
      "type": "bus_stop",
      "name": "Beechurst Road - S-bound",
      "latitude": 53.39014,
      "longitude": -2.18307,
      "accuracy": 20,
      "description": "Adswood",
      "atcocode": "1800SG08021"
    }
  ]
}'''))
                        .withHeader(CONTENT_TYPE, JSON_UTF_8 as String))
        mockServerClient.when(request('/places.json').withQueryStringParameters(query: ['notFound'],
                type: ['bus_stop'],
                app_key: ['transportApiApplicationKey'],
                app_id: ['transportApiApplicationId']))
                .respond(response().withBody(JsonBody.json('''\
{
  "request_time": "2021-02-10T00:20:47+00:00",
  "source": "",
  "acknowledgements": "",
  "member": []
}'''))
                        .withHeader(CONTENT_TYPE, JSON_UTF_8 as String))
    }

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
