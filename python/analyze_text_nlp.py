import os
import sys
import pandas as pd
import MeCab
import re

# Загрузка словаря частотности японских слов из XLSX
def load_common_words(filepath):
    df = pd.read_excel(filepath)
    common_words = set(df['レマ'].tolist())
    return common_words

# Функция для токенизации текста на японском и фильтрации
def tokenize_and_filter(text, mecab):
    node = mecab.parseToNode(text)
    words = []
    while node:
        if node.surface:
            features = node.feature.split(',')
            pos = features[0]
            lemma = features[6] if len(features) > 6 and features[6] != '*' else node.surface

            # Исключаем пунктуацию и частицы
            if pos not in ('記号', '助詞', '助動詞', '接続詞') and pos:
                # Используем лемму (базовую форму) вместо поверхности
                words.append({'surface': node.surface, 'pos': pos, 'lemma': lemma})
        node = node.next
    return words

# Функция для извлечения фраз (фокус на редких и длинных словах)
def extract_phrases(words, common_words):
    phrases = []
    current_phrase = []

    for word_data in words:
        lemma = word_data['lemma']
        pos = word_data['pos']

        if lemma not in common_words and len(lemma) > 1 and pos not in ('記号', '助詞', '助動詞', '接続詞'):
            current_phrase.append(lemma)
        else:
            if current_phrase:
                phrases.append(''.join(current_phrase))
                current_phrase = []

    if current_phrase:
        phrases.append(''.join(current_phrase))

    return list(set(phrases))  # убираем дубликаты

# Основная функция для анализа текста и выделения редких слов и фраз
def analyze_text(text, common_words, mecab):
    words_data = tokenize_and_filter(text, mecab)
    words = [word['lemma'] for word in words_data if word['lemma'] and word['lemma'] not in common_words and len(word['lemma']) > 1]

    # Идентифицируем редкие слова
    rare_words = list(set(words))

    # Извлечение научных фраз
    phrases = extract_phrases(words_data, common_words)

    return rare_words, phrases

# Получаем директорию текущего скрипта
current_script_dir = os.path.dirname(os.path.abspath(__file__))

# Формируем полный путь к файлу
xlsx_filepath = os.path.join(current_script_dir, 'NLT1.40_freq_list.xlsx')

# Загрузка часто используемых слов
common_words = load_common_words(xlsx_filepath)

# Инициализация MeCab
mecab = MeCab.Tagger()

# Получение текстового аргумента из командной строки
if len(sys.argv) < 2:
    print("Usage: python script.py \"<text>\"")
    sys.exit(1)

# Объединение всех аргументов в один текст
input_text = " ".join(sys.argv[1:])

# Анализ текста
rare_words, phrases = analyze_text(input_text, common_words, mecab)

# Вывод результатов
if rare_words:
    print("Rare Words:")
    print("、".join(rare_words) + "\n")
else:
    print("No rare words found.\n")

if phrases:
    print("Scientific Phrases:")
    print("、".join(phrases) + "\n")
else:
    print("No scientific phrases found.\n")

output_path = './temp/analyzed_text.txt'
with open(output_path, "w", encoding="utf-8") as f:
    f.write("Rare Words:\n")
    f.write(", ".join(rare_words) + "\n\n")
    f.write("Phrases:\n")
    f.write(", ".join(phrases) + "\n")