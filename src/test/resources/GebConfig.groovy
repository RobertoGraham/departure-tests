import org.openqa.selenium.chrome.ChromeOptions
import org.testcontainers.containers.BrowserWebDriverContainer
import org.testcontainers.containers.Network

departureAppNetworkAlias = 'departure-app'

network = Network.newNetwork()

driver = {
    new BrowserWebDriverContainer<>()
            .withCapabilities(new ChromeOptions().tap {
                args = ["--unsafely-treat-insecure-origin-as-secure=http://$departureAppNetworkAlias/"]
            })
            .withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.SKIP, null)
            .withNetwork(network as Network)
            .tap {
                start()
            }.webDriver
}