## Inspiration

Do you have parents who struggle with technology and have fallen victim to phishing scams? Or perhaps you know a friend or relative who fits this description? If so, this app is perfect for them!

As someone whose parents are not proficient with computers and English is not their first language, I've found myself helping them with numerous chargebacks over the years due to text message and email phishing scams. I often receive frantic calls from them saying things like:

"Please help me! There's a $50 charge on my bank account that I didn't authorize."

And then I ask them:

"Mom, did you click on a link that prompted you to sign into your Wells Fargo account because it claimed there was an unauthorized $400 purchase?"

Unfortunately, with the increasing prevalence of AI, these scams are only going to become more sophisticated. That's why we need to fight fire with fire and use AI to protect ourselves!

Our app is designed to make it incredibly simple for individuals who struggle with technology to identify phishing scams before they fall victim to them. All you need to do is upload a screenshot of the suspicious text message, email, or any other suspicious content, and it will be analyzed using a combination of powerful services, including ChatGPT's API, an AWS Sagemaker ML model called Mphasis DeepInsights Document Classifier, and three additional APIs. This comprehensive analysis ensures an accurate detection of whether the content is unsafe or potentially a scam.

## How we built it

Unfortunately, as of now, ChatGPT 3.5 and 4 are unable to process images. Similarly, the ML model and other APIs also lack this capability. However, we've devised an innovative solution to address this limitation by leveraging AWS Textract. We extract the text from the screenshots using Textract and utilize it for analysis with the other APIs.

Our app is built on Android using Java and RxJava. To extract text from screenshots, we utilize the AWS Textract Java SDK. API Gateway and a nodejs Lambda Function was used to interact with the Sagemaker model.

We have incorporated five different methods for scam detection:

    ChatGPT 3.5 API: We leverage the power of ChatGPT 3.5 to analyze and detect potential scams. (Please note that GPT4 does not currently offer an API.)

    AWS Marketplace SageMaker Model - Mphasis DeepInsights Document Classifier: This model, available on the AWS Marketplace, provides advanced document classification capabilities that enable us to identify potential scams more accurately.

    RapidAPI Scam Email/Phone APIs:
        Fraud Freeze API: With this API, we can detect fraudulent activities and scams related to freezing accounts.
        Disposable Emailer API: This API helps identify disposable or temporary email addresses commonly used in phishing attempts.
        oopScam API: By leveraging this API, we can detect various types of scams, providing an additional layer of protection.

By combining the power of these five methods, we ensure a robust scam detection system within our app.

## Links

https://rapidapi.com/oopspam/api/oopspam-spam-filter

https://rapidapi.com/xand3rr/api/fraudfreeze-phishing-check/

https://rapidapi.com/jibr/api/disposable-email-validation

https://aws.amazon.com/marketplace/ai/procurement?productId=d7631b0f-e5fb-4daf-bbe0-55f92710e343

https://openai.com/blog/openai-api

