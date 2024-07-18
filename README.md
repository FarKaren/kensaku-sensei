# Приложение "Kensaku-sensei" на Ubuntu 22.04

## Этапы для настройки и запуска приложения

### Шаг 1: Создание папки
Создайте папку kensakusensei в домашней директории пользователя.
mkdir ~/kensakusensei

Пример: home/user/kensakusensei

### Шаг 2: Добавление приложения
Добавьте в папку kensakusensei само приложение. Это можно сделать двумя способами:

- Скопировать JAR файл

  Скачайте JAR файл по следующему адресу:
  https://github.com/People-Cloud/izumohack2024_textbook_translator/tree/main/backend/jar
  и скопируйте его в папку kensakusensei.

- Склонировать проект

  Склонируйте проект из репозитория:
  git clone https://github.com/People-Cloud/izumohack2024_textbook_translator/tree/main/backend ~/kensakusensei

### Шаг 3: Скопировать папку "python"
Скопируйте папку python из репозитория:
git clone https://github.com/People-Cloud/izumohack2024_textbook_translator/tree/main/backend/python ~/kensakusensei/python

### Шаг 4: Добавить пустую папку "files"
Создайте пустую папку files внутри kensakusensei.
mkdir ~/kensakusensei/files

### Шаг 5: Копирование папки "myenv"
Скопируйте папку myenv в домашнюю директорию пользователя из следующего репозитория:
git clone https://github.com/People-Cloud/izumohack2024_textbook_translator/tree/main/backend/myenv ~/myenv

### Шаг 6: Установить Tesseract
Установите Tesseract и добавьте языковые файлы.
sudo apt-get install tesseract-ocr
В директории usr/share должна появиться папка tesseract-ocr.
Скопируйте 3 языковых файла из следующего репозитория:
https://github.com/People-Cloud/izumohack2024_textbook_translator/tree/main/backend/tesseract
в папку /usr/share/tesseract-ocr/4.00/tessdata/.

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
        
### Шаг 7: Запуск приложения
- Если скопировали JAR файл (Шаг 2):
  Откройте консоль в папке home/user/kensakusensei и выполните команду:
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


Example: home/user/kensakusensei

### Step 2: Add the application
Add the application to the kensakusensei folder. This can be done in two ways:

- Copy the JAR file

  Download the JAR file from the following link:  
  JAR file (https://github.com/People-Cloud/izumohack2024_textbook_translator/tree/main/backend/jar)  
  and copy it to the kensakusensei folder.

- Clone the project

  Clone the project from the repository:
  
  git clone https://github.com/People-Cloud/izumohack2024_textbook_translator/tree/main/backend ~/kensakusensei
  

### Step 3: Copy the "python" folder
Copy the python folder from the repository:
git clone https://github.com/People-Cloud/izumohack2024_textbook_translator/tree/main/backend/python ~/kensakusensei/python


### Step 4: Add an empty "files" folder
Create an empty files folder inside kensakusensei.
mkdir ~/kensakusensei/files


### Step 5: Copy the "myenv" folder
Copy the myenv folder to the user's home directory from the following repository:
git clone https://github.com/People-Cloud/izumohack2024_textbook_translator/tree/main/backend/myenv ~/myenv


### Step 6: Install Tesseract
Install Tesseract and add the language files.
sudo apt-get install tesseract-ocr


The tesseract-ocr folder should appear in the /usr/share/ directory.

Copy 3 language files from the following repository:  
Language files (https://github.com/People-Cloud/izumohack2024_textbook_translator/tree/main/backend/tesseract)  
to the folder /usr/share/tesseract-ocr/4.00/tessdata/.

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

Using these methods, you can more precisely control the formatting and alignment of text in your Markdown document.





   
