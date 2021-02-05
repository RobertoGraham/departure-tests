package io.github.robertograham.departuretests.page

import geb.Module

final class LineCard extends Module {

    static content = {
        name { $('h2').text() }
        operator { $('h3').text() }
        departures { $('ul li').moduleList Departure }
    }
}
