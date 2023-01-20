package io.github.robertograham.departuretests.page

import geb.Module

final class LineCard extends Module {

    static content = {
        name { $('h3').text() }
        operator { $('h4').text() }
        departures { $('ul li').moduleList Departure }
    }
}
