import org.sablecc.sablecc.node.*;
import org.sablecc.sablecc.analysis.Analysis;
import org.sablecc.sablecc.node.Package;  // avoid name conflicts with java.lang.Package

public aspect NodesAspect {

    pointcut bangAlt(): target(Node) && !target(Alt) && call(* *(..));
    pointcut bangAltName(): target(Node) && !target(AltName) && call(* *(..));
    pointcut bangAltNameOpt(): target(Node) && !target(AltNameOpt) && call(* *(..));
    pointcut bangAlts(): target(Node) && !target(Alts) && call(* *(..));
    pointcut bangAltsTail(): target(Node) && !target(AltsTail) && call(* *(..));
    pointcut bangAltsTails(): target(Node) && !target(AltsTails) && call(* *(..));
    pointcut bangBasic(): target(Node) && !target(Basic) && call(* *(..));
    pointcut bangBinOp(): target(Node) && !target(BinOp) && call(* *(..));
    pointcut bangConcat(): target(Node) && !target(Concat) && call(* *(..));
    pointcut bangElem(): target(Node) && !target(Elem) && call(* *(..));
    pointcut bangElemName(): target(Node) && !target(ElemName) && call(* *(..));
    pointcut bangElemNameOpt(): target(Node) && !target(ElemNameOpt) && call(* *(..));
    pointcut bangElems(): target(Node) && !target(Elems) && call(* *(..));
    pointcut bangGrammar(): target(Node) && !target(Grammar) && call(* *(..));
    pointcut bangHelperDef(): target(Node) && !target(HelperDef) && call(* *(..));
    pointcut bangHelperDefs(): target(Node) && !target(HelperDefs) && call(* *(..));
    pointcut bangHelpers(): target(Node) && !target(Helpers) && call(* *(..));
    pointcut bangHelpersOpt(): target(Node) && !target(HelpersOpt) && call(* *(..));
    pointcut bangIdList(): target(Node) && !target(IdList) && call(* *(..));
    pointcut bangIdListOpt(): target(Node) && !target(IdListOpt) && call(* *(..));
    pointcut bangIdListTail(): target(Node) && !target(IdListTail) && call(* *(..));
    pointcut bangIdListTails(): target(Node) && !target(IdListTails) && call(* *(..));
    pointcut bangIgnTokens(): target(Node) && !target(IgnTokens) && call(* *(..));
    pointcut bangIgnTokensOpt(): target(Node) && !target(IgnTokensOpt) && call(* *(..));
    pointcut bangLookAhead(): target(Node) && !target(LookAhead) && call(* *(..));
    pointcut bangLookAheadOpt(): target(Node) && !target(LookAheadOpt) && call(* *(..));
    pointcut bangPackage(): target(Node) && !target(Package) && call(* *(..));
    pointcut bangPackageOpt(): target(Node) && !target(PackageOpt) && call(* *(..));
    pointcut bangPChar(): target(Node) && !target(PChar) && call(* *(..));
    pointcut bangPkgId(): target(Node) && !target(PkgId) && call(* *(..));
    pointcut bangPkgName(): target(Node) && !target(PkgName) && call(* *(..));
    pointcut bangPkgNameOpt(): target(Node) && !target(PkgNameOpt) && call(* *(..));
    pointcut bangPkgNameTail(): target(Node) && !target(PkgNameTail) && call(* *(..));
    pointcut bangPkgNameTails(): target(Node) && !target(PkgNameTails) && call(* *(..));
    pointcut bangProd(): target(Node) && !target(Prod) && call(* *(..));
    pointcut bangProds(): target(Node) && !target(Prods) && call(* *(..));
    pointcut bangProductions(): target(Node) && !target(Productions) && call(* *(..));
    pointcut bangProductionsOpt(): target(Node) && !target(ProductionsOpt) && call(* *(..));
    pointcut bangPSet(): target(Node) && !target(PSet) && call(* *(..));
    pointcut bangRegExp(): target(Node) && !target(RegExp) && call(* *(..));
    pointcut bangRegExpTail(): target(Node) && !target(RegExpTail) && call(* *(..));
    pointcut bangRegExpTails(): target(Node) && !target(RegExpTails) && call(* *(..));
    pointcut bangSpecifier(): target(Node) && !target(Specifier) && call(* *(..));
    pointcut bangSpecifierOpt(): target(Node) && !target(SpecifierOpt) && call(* *(..));
    pointcut bangStart(): target(Node) && !target(Start) && call(* *(..));
    pointcut bangStateList(): target(Node) && !target(StateList) && call(* *(..));
    pointcut bangStateListOpt(): target(Node) && !target(StateListOpt) && call(* *(..));
    pointcut bangStateListTail(): target(Node) && !target(StateListTail) && call(* *(..));
    pointcut bangStateListTails(): target(Node) && !target(StateListTails) && call(* *(..));
    pointcut bangStates(): target(Node) && !target(States) && call(* *(..));
    pointcut bangStatesOpt(): target(Node) && !target(StatesOpt) && call(* *(..));
    pointcut bangToken(): target(Node) && !target(Token) && call(* *(..));
    pointcut bangTokenDef(): target(Node) && !target(TokenDef) && call(* *(..));
    pointcut bangTokenDefs(): target(Node) && !target(TokenDefs) && call(* *(..));
    pointcut bangTokens(): target(Node) && !target(Tokens) && call(* *(..));
    pointcut bangTokensOpt(): target(Node) && !target(TokensOpt) && call(* *(..));
    pointcut bangTransition(): target(Node) && !target(Transition) && call(* *(..));
    pointcut bangTransitionOpt(): target(Node) && !target(TransitionOpt) && call(* *(..));
    pointcut bangUnExp(): target(Node) && !target(UnExp) && call(* *(..));
    pointcut bangUnExps(): target(Node) && !target(UnExps) && call(* *(..));
    pointcut bangUnOp(): target(Node) && !target(UnOp) && call(* *(..));
    pointcut bangUnOpOpt(): target(Node) && !target(UnOpOpt) && call(* *(..));
}

    
