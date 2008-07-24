public aspect Asp {
  before(): execution(new(@Ann (*),..)) {}
}
