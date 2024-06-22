# Emergency Report
Emergency Report este o aplicație de transmitere a alertelor de urgență, disponibilă [aici](https://github.com/cata-upt/EmergencyReport.git).

## Cerințe preliminare
### Descărcarea Java JDK 17 și Android Studio
Pentru a rula aplicația e necesară instalarea [Git](https://git-scm.com/downloads), [Java JDK 17](https://www.oracle.com/java/technologies/downloads/#java17) și [Android Studio](https://developer.android.com/studio) urmând pașii descriși în documentele oficiale. La prima deschidere, Android Studio va descărca și configura SDK-ul Android necesar.

##Deschiderea proiectului în Android Studio
Pentru a deschide și rula aplicația în Android Studio este nevoie de realizarea următorilor pași:
1. Clonarea repository-ului în folderul în care urmează să se afle aplicația, prin rularea comenzii
```git
git clone https://github.com/cata-upt/EmergencyReport.git
```
2. Deschiderea Android Studio
3. Importarea proiectului prin selectarea „File” > „Open”, apoi navigarea la directorul unde a fost clonat proiectul și selectarea acestuia. Pentru a deschide proiectul se apasă OK.
4. Pentru configurarea JDK 17 în Android Studio, se accesează „File” > „Project Structure”, iar în secțiunea SDK Location, se setează calea către JDK 17 instalat. De exemplu:
```
/path/to/jdk-17
```
5. Pentru sincronizarea proiectului se selectează  „File” > „Sync Project with Gradle Files”.
