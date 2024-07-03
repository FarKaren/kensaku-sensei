# Базовый образ для фазы сборки
FROM gradle:7.6.1-jdk17-focal AS builder
MAINTAINER Karen Farmanyan <farkar160@gmail.com>

COPY . /app
WORKDIR /app

# Собираем проект с использованием Gradle
RUN gradle bootJar

# Базовый образ для конечного контейнера на основе Ubuntu 22.04
FROM ubuntu:22.04

# Отключаем интерактивный режим
ENV DEBIAN_FRONTEND=noninteractive

# Обновляем и устанавливаем необходимые зависимости
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    python3 \
    python3-venv \
    python3-pip \
    wget \
    unzip \
    tesseract-ocr \
    curl \
    jq \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Устанавливаем дополнительные языковые файлы для Tesseract
RUN wget https://github.com/tesseract-ocr/tessdata/raw/main/rus.traineddata -P /usr/share/tesseract-ocr/4.00/tessdata/ \
    && wget https://github.com/tesseract-ocr/tessdata/raw/main/jpn.traineddata -P /usr/share/tesseract-ocr/4.00/tessdata/ \
    && wget https://github.com/tesseract-ocr/tessdata/raw/main/por.traineddata -P /usr/share/tesseract-ocr/4.00/tessdata/ \
    && wget https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata -P /usr/share/tesseract-ocr/4.00/tessdata/

# Устанавливаем Google Chrome и ChromeDriver
RUN CHROME_VERSION=$(curl -s https://googlechromelabs.github.io/chrome-for-testing/last-known-good-versions-with-downloads.json | jq -r '.channels.Stable.version') \
    && wget -O chrome-linux64.zip "https://storage.googleapis.com/chrome-for-testing-public/${CHROME_VERSION}/linux64/chrome-linux64.zip" \
    && unzip chrome-linux64.zip -d /usr/local/bin/ \
    && ln -s /usr/local/bin/chrome-linux64/chrome /usr/local/bin/chrome \
    && wget -O chromedriver-linux64.zip "https://storage.googleapis.com/chrome-for-testing-public/${CHROME_VERSION}/linux64/chromedriver-linux64.zip" \
    && unzip chromedriver-linux64.zip -d /usr/local/bin/ \
    && ln -s /usr/local/bin/chromedriver-linux64/chromedriver /usr/local/bin/chromedriver \
    && chmod +x /usr/local/bin/chromedriver \
    && rm chrome-linux64.zip chromedriver-linux64.zip

# Создаем и активируем виртуальное окружение для Python
RUN python3 -m venv /opt/myenv
RUN /bin/bash -c "source /opt/myenv/bin/activate \
    && pip install --upgrade pip \
    && pip install transformers \
    && pip install nltk \
    && pip install mecab-python3 \
    && pip install openpyxl"

# Копируем собранный проект из фазы сборки
COPY --from=builder /app/build/libs/*.jar /app/app.jar

# Экспонируем порт
EXPOSE 9900/tcp

# Устанавливаем переменные окружения для Chrome
ENV PATH=$PATH:/usr/local/bin
ENV JAVA_OPTS="-XX:+UseContainerSupport"

# Указываем рабочий каталог
WORKDIR /app

# Команда для запуска приложения Spring Boot
ENTRYPOINT ["/bin/sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
