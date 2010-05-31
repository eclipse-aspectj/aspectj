public aspect MinimalExample {
    declare @type : @AnnotationWithParam("arg") * : @AnnotationWithParam2("gra");
    // the following lines leads to a misleading compiler error
    declare @type : @AnnotationWithParamAndTypo("arg") * : @AnnotationWithParam2("gra");
}

@interface AnnotationWithParam {
    String value();
}

@interface AnnotationWithParam2 {
    String value();
}
