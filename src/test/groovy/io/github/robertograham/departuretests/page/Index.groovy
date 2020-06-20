package io.github.robertograham.departuretests.page

import geb.Page

final class Index extends Page {

    static url = ''

    static content = {
        busStops {
            $('div', class: 'mdc-card').moduleList BusStop
        }
    }

    static at = {
        waitFor {
            !busStops.isEmpty()
        }
    }
}