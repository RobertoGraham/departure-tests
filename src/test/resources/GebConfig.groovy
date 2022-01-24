import org.openqa.selenium.chrome.ChromeOptions
import org.testcontainers.containers.BrowserWebDriverContainer
import org.testcontainers.containers.Network

departureAppNetworkAlias = 'departure-app'

baseUrl = "http://$departureAppNetworkAlias/"

network = Network.newNetwork()

driver = {
    new BrowserWebDriverContainer<>()
            .withCapabilities(new ChromeOptions().addArguments("--unsafely-treat-insecure-origin-as-secure=$baseUrl"))
            .withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.SKIP, null)
            .withNetwork(network as Network)
            .tap {
                start()
            }.webDriver
}
