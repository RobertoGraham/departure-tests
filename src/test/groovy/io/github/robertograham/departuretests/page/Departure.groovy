package io.github.robertograham.departuretests.page

import geb.Module

final class Departure extends Module {

    static content = {
        time { $('span', class: 'mdc-list-item__primary-text').text() }
        destination { $('span', class: 'mdc-list-item__secondary-text').text() }
    }
}
