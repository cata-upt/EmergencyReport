# Emergency Report
Emergency Report este o aplicație de transmitere a alertelor de urgență, disponibilă [aici](https://github.com/cata-upt/EmergencyReport.git).

## Cerințe preliminare
### Descărcarea Java JDK 17 și Android Studio
Pentru a rula aplicația e necesară instalarea [Git](https://git-scm.com/downloads), [Java JDK 17](https://www.oracle.com/java/technologies/downloads/#java17) și [Android Studio](https://developer.android.com/studio) urmând pașii descriși în documentele oficiale. La prima deschidere, Android Studio va descărca și configura SDK-ul Android necesar.

## Deschiderea proiectului în Android Studio
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

## Rularea aplicației
### 1. Conectarea unui dispozitiv Android fizic
#### Pasul 1: Activarea Opțiunilor pentru dezvoltatori
1. Deschiderea Setărilor dispozitivului Android.
2. Navigarea la Despre telefon (sau Despre tabletă).
3. Găsirea Numărului de build și apăsarea pe el de 7 ori. Utilizatorul va vedea un mesaj care indică activarea opțiunilor pentru dezvoltatori.
#### Pasul 2: Activarea Depanării USB
1. Deschiderea Setărilor și navigarea la „Sistem” > „Avansat” > „Opțiuni pentru dezvoltatori”.
2. Activarea Depanării USB.
#### Pasul 3: Conectarea dispozitivului la computer
1. Conectarea dispozitivului Android la computer folosind un cablu USB.
2. Acceptarea solicitării de autorizare pe dispozitivul Android.
#### Pasul 4: Verificarea conexiunii în Android Studio
1. Deschiderea Android Studio.
2. Selectarea Run > Run „app”.
3. Selectarea dispozitivului Android din lista de dispozitive disponibile și apăsarea pe OK.
### 2. Utilizarea unui emulator Android
#### Pasul 1: Configurarea unui emulator în Android Studio
1. Deschiderea Android Studio.
2. Navigarea la Tools > AVD Manager (Android Virtual Device Manager).
3. Click pe Create Virtual Device.
#### Pasul 2: Selectarea dispozitivului virtual
1. Alegerea unui dispozitiv din lista de dispozitive disponibile (de exemplu, Pixel 4).
2. Click pe Next.
#### Pasul 3: Selectarea unei imagini de sistem
1. Alegerea unei imagini de sistem (de exemplu, Q pentru Android 10.0) și click pe Download dacă nu este deja descărcată.
2. Click pe Next după ce imaginea de sistem este descărcată și instalată.
#### Pasul 4: Configurarea emulatorului
1. Revizuirea configurației emulatorului și click pe Finish.
#### Pasul 5: Rularea emulatorului
1. În Android Studio, deschiderea AVD Manager din nou.
2. Click pe butonul Play (triunghi verde) de lângă dispozitivul virtual creat pentru a porni emulatorul.
#### Pasul 6: Rularea aplicației pe emulator
1. Selectarea Run > Run „app” în Android Studio.
2. Selectarea emulatorului din lista de dispozitive disponibile și apăsarea pe OK.
