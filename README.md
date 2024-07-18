# Приложение "Kensaku-sensei" на Ubuntu 22.04

## Этапы для настройки и запуска приложения

### Шаг 1: Создание папки
Создайте папку kensakusensei в домашней директории пользователя.
mkdir ~/kensakusensei

Пример: home/user/kensakusensei

### Шаг 2: Разархивируйте файл 
https://github.com/People-Cloud/izumohack2024_textbook_translator/tree/main/backend/environments.tar.gz <br>
Внутри 4 папки: jar, python, myenv, tesseract.

### Шаг 3: Добавление приложения
Добавьте в папку kensakusensei само приложение. Это можно сделать двумя способами:

- Скопировать JAR файл
  Из папки jar скопировать jar файл kensaku-sensei-0.0.1-SNAPSHOT.jar в папку kensakusensei.

- Склонировать проект
  Склонируйте проект из репозитория:
  git clone https://github.com/People-Cloud/izumohack2024_textbook_translator/tree/main/backend ~/kensakusensei

### Шаг 4: Скопировать папку "python"
Скопируйте папку python в ~/kensakusensei/python

### Шаг 5: Добавить пустую папку "files"
Создайте пустую папку files внутри kensakusensei. <br>
mkdir ~/kensakusensei/files

### Шаг 6: Копирование папки "myenv"
Скопируйте папку myenv в домашнюю директорию пользователя ~/myenv

### Шаг 7: Установить Tesseract
Установите Tesseract и добавьте языковые файлы. <br>
sudo apt-get install tesseract-ocr <br>
В директории usr/share должна появиться папка tesseract-ocr. <br>
Скопируйте 3 языковых файла из папки tesseract в папку /usr/share/tesseract-ocr/4.00/tessdata/. <br>

### Дерево директорий
Ваше дерево директорий должно выглядеть следующим образом:<br>
```
home
└── user
    ├── myenv
    └── kensakusensei
        ├── python
        ├── files
        └── kensaku-sensei (или kensaku-sensei-0.0.1-SNAPSHOT.jar)
```
        
### Шаг 8: Запуск приложения
- Если скопировали JAR файл (Шаг 2):
  Откройте консоль в папке home/user/kensakusensei и выполните команду: <br>
  java -jar kensaku-sensei-0.0.1-SNAPSHOT.jar

- Если склонировали проект (Шаг 2):
  Установите одну из сред разработки, например Intellij IDEA или Eclipse.
  Откройте склонированный проект в установленной среде разработки и запустите метод main в классе TranslateHelperApplication.

<br>
<br>
<br>

# Application "Kensaku-sensei" on Ubuntu 22.04

## Steps for setting up and running the application

### Step 1: Create a folder
Create a kensakusensei folder in the user's home directory.
mkdir ~/kensakusensei

### Step 2: Unzip file
https://github.com/People-Cloud/izumohack2024_textbook_translator/tree/main/backend/environments.tar.gz  <br>
Inside 4 folders: jar, python, myenv, tesseract.

Example: home/user/kensakusensei

### Step 3: Add the application
Add the application to the kensakusensei folder. This can be done in two ways:

- Copy the JAR file
  From jar folder copy jar file kensaku-sensei-0.0.1-SNAPSHOT.jar to the kensakusensei folder.

- Clone the project
  Clone the project from the repository:
  git clone https://github.com/People-Cloud/izumohack2024_textbook_translator/tree/main/backend ~/kensakusensei
  
### Step 4: Copy the "python" folder
Copy the python folder to ~/kensakusensei/python

### Step 5: Add an empty "files" folder
Create an empty files folder inside kensakusensei.
mkdir ~/kensakusensei/files

### Step 6: Copy the "myenv" folder
Copy the myenv folder to the user's home directory ~/myenv

### Step 7: Install Tesseract
Install Tesseract and add the language files. <br>
sudo apt-get install tesseract-ocr <br>
The tesseract-ocr folder should appear in the /usr/share/ directory. <br>
Copy 3 language files from the folder tesseract to the folder /usr/share/tesseract-ocr/4.00/tessdata/. <br>

### Directory Tree
Your directory tree should look like
```
home
└── user
    ├── myenv
    └── kensakusensei
        ├── python
        ├── files
        └── kensaku-sensei (or kensaku-sensei-0.0.1-SNAPSHOT.jar)
```

### Step 8: Run the application
- If you copied the JAR file (Step 2):
  Open a terminal in the home/user/kensakusensei folder and run the command:
  java -jar kensaku-sensei-0.0.1-SNAPSHOT.jar
  
- If you cloned the project (Step 2):  
  Install an IDE like Intellij IDEA or Eclipse. Open the cloned project in the IDE and run the main method in the TranslateHelperApplication class.

---





   
