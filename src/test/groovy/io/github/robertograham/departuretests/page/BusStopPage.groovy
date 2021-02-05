package io.github.robertograham.departuretests.page

import geb.Page

final class BusStopPage extends Page {

    static content = {
        name { $('header h2').text() }
        locality { $('header h3').text() }
        lines { $('div', class: 'mdc-card').moduleList LineCard }
    }

    static at = {
        waitFor {
            !lines.empty
        }
    }
}
