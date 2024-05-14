package pl.interpreter

class TestUtils {

    var static final float DELTA = 0.01f;

    static boolean isClose(float a, float b) {
        return Math.abs(a - b) <= DELTA
    }
}
