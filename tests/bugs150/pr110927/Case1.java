interface I {
}

interface IE1<T> {

}

interface IE2 {

}

aspect A1 {
       declare parents : I implements  IE1<String>;
}

aspect A2 {
       declare parents : I implements  IE2;
}