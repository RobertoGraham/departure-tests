package io.github.robertograham.departuretests.page

import geb.Module
import org.apache.commons.lang3.StringUtils

final class BusStopCard extends Module {

    static content = {
        busStopPageLink(to: BusStopPage) { $ 'a', class: 'mdc-card__primary-action' }
        id { StringUtils.substringAfterLast(StringUtils.substringBeforeLast(busStopPageLink.attr('href'), '/'), '/') }
        name { $('h2').text() }
        locality { $('h3').text() }
    }
}
