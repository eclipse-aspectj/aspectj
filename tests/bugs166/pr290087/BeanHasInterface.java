public aspect BeanHasInterface {
  declare parents : Bean implements Interface;
  declare parents : BeanChild extends InterfaceParent<Bean>;
}

