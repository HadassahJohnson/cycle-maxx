## **An AI-Powered Personal Trainer That Works *with* Your Cycle!**

## **Inspiration**

Although improving, women’s health isn’t typically considered in day-to-day activities—especially when it comes to the menstrual cycle. Hormonal fluctuations throughout a single cycle can greatly affect one’s mood, energy, and motivation.

### Our Solution

We decided to develop CycleMaxx (cycleMAXX) to help mitigate these inconsistencies when it comes to exercising. Exercising is a crucial component of women’s physical and mental health, but consistency becomes challenging at different phases of the menstrual cycle.

### What It Does

cycleMAXX uses your period history (or last known period date) and biometric data from Presage's _SmartSpectra_ to identify what phase of the menstrual cycle users are in. Based on that data, Gemini API will develop a customized workout plan for the week (including factors such as the user's desired frequency, preferred activity categories, fitness goals, and experience level) and let the user manually edit if necessary.

## **What We Learned**

As we worked on this app, we learned a significant amount about the timing of menstrual cycle phases with energy and hormonal levels. Since we planned to implement Presage’s _SmartSpectra SDK_, we also learned about correlations between heart rate and blood pressure with the menstrual cycle.

## **Building cycleMAXX**

We built our app with Android Studio, written in Kotlin. It took a few hours for us to plan and solidify our approach. We created brief requirements, a logo, and UML diagrams for clarity. We split out our roles based on our backgrounds and what we wanted to learn. After that, we were ready to dive into our project.

We started out writing in C++ until we realized, an unfortunate 7 hours later, that C++ doesn’t support the necessary camera capabilities for _SmartSpectra_. After that, we remained persistent and switched to Android Studio. With the help of Gemini, we used Firebase Kotlin SDK to develop the backend and Jetpack Compose for the UI. We were ultimately unable to implement Presage into our prototype, but thankfully, Gemini was able to help implement Gemini API.

## **Challenges**

Implementation is never as easy as ideation. Although it is planned for our future product, we were unable to implement Presage’s _SmartSpectra SDK_ due to connection constraints. As students from outside of RIT, we were unable to get a secure connection, and the Android emulator did not function properly. After starting with the C++ SDK, we attempted to switch to their Android SDK, but again, that had issues related to connectivity and stability.

We learned the hard lesson that plans don’t always go as expected, but it’s necessary to adapt quickly. We still would love to work with Presage in the future, but we will need a more stable system to support our product before we ship to production.

## **Next Steps**

We’ve achieved a lot in under 24 hours, but we still have many items we’d like to work on next, including:

* Implementation of Presage’s _SmartSpectra SDK_

* Clean up source code

* Gamify user progress and streaks

* Include other significant life stages in our tracking, including pregnancy, peri-menopause, menopause, and post-menopause

Check out our pitch deck for more details: [cycleMAXX Pitch Deck](https://www.canva.com/design/DAHCqKQc5uM/ZKBTpKpOqwCnFD2UWeY9rw/view?utm_content=DAHCqKQc5uM)

We’re looking forward to taking cycleMAXX further!
