package io.github.robertograham.departuretests.page

import geb.Module
import org.apache.commons.lang3.StringUtils

class BusStop extends Module {

    static content = {
        departuresLink { $ 'a', class: 'mdc-card__primary-action' }
        id { StringUtils.substringAfterLast(StringUtils.substringBeforeLast(departuresLink.attr('href'), '/'), '/') }
    }
}
