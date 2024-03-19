package pl.interpreter

import spock.lang.Specification

class TokenPatternsProviderSpec extends Specification {

    def 'Should provide correct identifier pattern'() {
        given:
        def pattern = TokenPatternsProvider.getIdentifierPattern();
        expect:
        pattern.matcher(str).matches() == matches
        where:
        str     << ["a", "", "abc", "1a", "#abc", "_"]
        matches << [true, false, true, false, false, true]
    }

}
