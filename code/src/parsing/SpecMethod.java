package parsing;

import java.util.List;
import java.util.Map;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

public class SpecMethod {
    
    String id;
    String[] parameterNames;
    Map<String, List<String[]>> calleeArguments;
    Map<String, List<String>> calleeReceivers, calleeConditions;
    
    public SpecMethod(String methodName, MethodDeclaration method) {
        this.id = methodName;
        this.parameterNames = new String[method.parameters().size()];
        for (int i = 0; i < method.parameters().size(); i++) {
            SingleVariableDeclaration d = (SingleVariableDeclaration) (method.parameters().get(i));
            this.parameterNames[i] = d.getName().getIdentifier();
        }
    }
}
