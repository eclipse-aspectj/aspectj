interface A {}
interface B {}
abstract aspect Parent< V extends A > {}
abstract aspect Child< V extends A & B > extends Parent< V > {}
