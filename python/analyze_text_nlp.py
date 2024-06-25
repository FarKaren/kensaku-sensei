import os
import sys
import string
from collections import Counter
import nltk
from nltk.corpus import stopwords, brown
from transformers import pipeline

# Загрузка необходимых пакетов
nltk.download('punkt')
nltk.download('brown')
nltk.download('averaged_perceptron_tagger')
nltk.download('stopwords')

# Проверка аргументов командной строки
if len(sys.argv) < 3:
    print("Usage: python analyze_text.py <text> <language>")
    sys.exit(1)

text = sys.argv[1]
lang = sys.argv[2]

# Загрузка модели NER
try:
    model_name = 'Jean-Baptiste/camembert-ner'
    ner_pipeline = pipeline('ner', model=model_name, tokenizer=model_name)
except Exception as e:
    print(f"Error loading model: {e}")
    sys.exit(1)

# Функция для токенизации текста
def tokenize(text):
    words = nltk.word_tokenize(text)
    table = str.maketrans('', '', string.punctuation)
    stripped = [w.translate(table) for w in words if w.isalpha()]
    return stripped

# Формирование списка частотности слова
def build_frequency_list():
    freq_list = nltk.FreqDist(brown.words())
    return freq_list

# Определение редких слов
def is_rare_word(word, frequency_list, threshold=10):
    return frequency_list[word.lower()] < threshold

# Токенизация текста
words = tokenize(text)

# Извлечение именованных сущностей
try:
    entities = ner_pipeline(text)
except Exception as e:
    print(f"Error during entity extraction: {e}")
    sys.exit(1)

print("Extracted Entities:")
for entity in entities:
    print(entity)

ignore_entities = {entity['word'] for entity in entities if entity['entity'] in {'I-PER', 'B-PER', 'I-ORG', 'B-ORG', 'I-LOC', 'B-LOC'}}

word_counts = Counter(words)

# Фильтрация слов
stop_words = set(stopwords.words('english'))
filtered_words = [
    word for word in words
    if not word.isupper()  # Игнорирование аббревиатур
    and not word.isdigit()  # Игнорирование чисел
    and word.lower() not in stop_words  # Убираем стоп-слова
    and word not in ignore_entities  # Игнорирование именованных сущностей
    and not word.istitle()  # Убираем заглавные слова, которые могут быть именами собственными
]

# Извлечение редких слов
frequency_list = build_frequency_list()
rare_words = [word for word in filtered_words if is_rare_word(word, frequency_list)]

# Комбинирование токенов
def combine_tokens(entities):
    combined_words = []
    current_word = ""
    for entity in entities:
        if entity['word'].startswith("▁"):  # Начало нового слова
            if current_word:
                combined_words.append(current_word)
                current_word = ""
            current_word += entity['word'].replace("▁", "")
        else:
            current_word += entity['word']
    if current_word:
        combined_words.append(current_word)
    return set(combined_words)

# Извлечение фраз
def extract_phrases(text, ignore_entities):
    words = nltk.word_tokenize(text)
    pos_tags = nltk.pos_tag(words)
    phrases = []
    current_phrase = []
    for (word, pos) in pos_tags:
        if word in ignore_entities:
            continue  # Пропускаем именованные сущности
        if pos.startswith('NN') or pos == 'JJ':
# Существительные и прилагательные
            current_phrase.append(word)
        else:
            if current_phrase:
                phrases.append(' '.join(current_phrase))
                current_phrase = []
    if current_phrase:
        phrases.append(' '.join(current_phrase))
    return list(set(phrases))  # Убираем дубликаты

# Основная часть
scientific_words = combine_tokens(entities)
phrases = extract_phrases(text, ignore_entities)

# Проверка текущей рабочей директории
current_directory = os.getcwd()
print(f"Текущая рабочая директория: {current_directory}")

# Дополнительная фильтрация научных слов перед выводом
def additional_filter(words):
    return [
        word for word in words
        if word.lower() not in stop_words  # Убираем стоп-слова (если вдруг они туда попали)
        and not word.istitle()  # Убираем заглавные слова, которые могут быть именами собственными
    ]

filtered_scientific_words = additional_filter(list(scientific_words))

# Дополнительная фильтрация редких слов
unique_rare_words = list(set(rare_words))

# Вывод результатов в консоль с фильтрацией
print("Scientific Words:")
print(", ".join(filtered_scientific_words) + "\n")

print("Rare Words:")
print(", ".join(unique_rare_words) + "\n")

print("Phrases:")
print(", ".join(set(phrases)) + "\n")

# Запись результатов в файл
output_path = './temp/analyzed_text.txt'
with open(output_path, "w", encoding="utf-8") as f:
    f.write("Scientific Words:\n")
    f.write(", ".join(filtered_scientific_words) + "\n\n")
    f.write("Rare Words:\n")
    f.write(", ".join(unique_rare_words) + "\n\n")
    f.write("Phrases:\n")
    f.write(", ".join(phrases) + "\n")