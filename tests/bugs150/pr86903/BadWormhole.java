
aspect BadWormhole {

  pointcut isDynamicService(Main mm,Service s):
    cflowbelow(this(mm)) && 
    if(true==true) && 
    this(s);

  //before(Main mm,Service s): isDynamicService(mm,s) {}
  before(Service s): isDynamicService(*,s) {}
}
