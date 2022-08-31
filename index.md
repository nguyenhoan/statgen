
<h2 align="center">Structural and Statistical Inference between API Documentation and Implementations, and Its Applications</h2>


<p style="text-align:justify">
API documentation is useful for developers to better understand how to correctly use the libraries. However, not all libraries provide good documentation on API usages. To provide better documentation, existing techniques have been proposed including program analysis-based and data mining-based approaches. In this work, we propose StatGen, a generative approach that generates behavioral exception documentation for any given code and vice versa. If the code does not have documentation, our result could help users in writing it. Otherwise, one could use StatGen to verify the consistency between implementations and documentation of the APIs. Specifically, we treat the problem of automatically generating documentation from a novel perspective: statistical machine translation (SMT). We consider the documentation and source code for an API method as the two abstraction levels of the same intention. Based on SMT, we propose a novel translation technique that makes use of the structure of source code and documentation under translation. 
</p>


<p style="text-align:justify">
We conducted several empirical experiments to intrinsically evaluate StatGen. We show that it is able to achieve high precision, 82% and 79%, and recall, 86% and 90%, when inferring the documentation from source code and vice versa, respectively. We also showed the usefulness and performance of our technique in two applications. In the first one, we used StatGen to generate the behavioral exception documentation for Apache APIs that lack of documentation by learning from the documentation of the equivalent APIs in JDK. The human subject rated 46% of the generated documentation as useful and 41% as some what useful. In the second application, we used StatGen to detect the inconsistency between the implementations and documentation on exception behaviors of several packages in JDK8. Our empirical results showed that our technique achieves high accuracy with both precision and recall over 90%, and outperforms the state-of-the-art technique. 
</p>
