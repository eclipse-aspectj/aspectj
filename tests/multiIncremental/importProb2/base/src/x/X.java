package x;

import java.util.ArrayList;

public aspect X {
//  before(): within(!X) {}
declare soft : IllegalAccessException : execution( * *(..) );

}
