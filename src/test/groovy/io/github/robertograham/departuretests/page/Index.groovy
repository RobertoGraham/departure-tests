package io.github.robertograham.departuretests.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole

final class Index {

    private final Page page
    private final Locator busStopPageLinks

    Index(final Page page) {
        this.page = page
        busStopPageLinks = page.getByRole(AriaRole.LINK)
    }

    void navigate() {
        page.navigate ''
        page.evaluate('''\
navigator.geolocation.getCurrentPosition = function (success, error) {
  success({
    coords: {
      longitude: 1.0,
      latitude: 1.0
    }
  });
}(pos => {})''')
        busStopPageLinks.waitFor()
    }

    List<BusStopCard> getBusStops() {
        (0..<busStopPageLinks.count()).collect { new BusStopCard(busStopPageLinks.nth(it)) }
    }
}
