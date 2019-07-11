package org.libelektra.model;

public class InjectionResult {
    private final InjectionDataResult injectionDataResult;
    private final SpecificationDataResult specificationDataResult;

    public InjectionResult(InjectionDataResult injectionDataResult,
                           SpecificationDataResult specificationDataResult) {
        this.injectionDataResult = injectionDataResult;
        this.specificationDataResult = specificationDataResult;
    }

    public boolean wasInjectionSuccessful() {
        return injectionDataResult.wasInjectionSuccessful();
    }

    public boolean errorCaughtBySpecification() {
        if (specificationDataResult == null) {
            return false;
        }
        return specificationDataResult.hasDetectedError();
    }

}
