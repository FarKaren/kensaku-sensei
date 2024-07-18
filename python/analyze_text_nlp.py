import pandas as pd
import MeCab
import re
import logging
import argparse
import unidic
import os

# Настройка логирования
logger = logging.getLogger()
logger.setLevel(logging.DEBUG)

# Создаем формат для логирования
formatter = logging.Formatter('%(asctime)s - %(levelname)s - %(message)s')

# Обработчик для логирования в консоль
console_handler = logging.StreamHandler()
console_handler.setLevel(logging.DEBUG)
console_handler.setFormatter(formatter)
logger.addHandler(console_handler)

# Порог частоты
TRESHOLD = 20000

# Функция для предобработки текста
def preprocess_text(text: str) -> str:
    text = re.sub(r'\s+', ' ', text)
    return text

# Проверка, является ли текст катаканой
def is_katakana(text: str) -> bool:
    return all('\u30A0' <= char <= '\u30FF' for char in text)

# Проверка, является ли текст японским
def is_japanese(text: str) -> bool:
    return all('\u3040' <= char <= '\u30FF' or '\u4E00' <= char <= '\u9FFF' for char in text)

# Проверка на валидность базовой формы
def is_valid_base_form(base_form: str) -> bool:
    if len(base_form) <= 1:
        return False
    if any(char.isdigit() for char in base_form):
        return False
    if not is_japanese(base_form):
        return False
    return not is_katakana(base_form)

def main(text: str):
    # Определение домашнего каталога пользователя
    home_dir = os.path.expanduser("~")
    # Построение полного пути к частотному словарю
    freq_path = os.path.join(home_dir, "kensakusensei/python/NLT1.40_freq_list.xlsx")
    # Построение полного пути к временной папке для записи результатов
    output_path = os.path.join(home_dir, "kensakusensei/files/analyzed_text.txt")

    # Чтение частотного словаря
    logging.info("Loading frequency dictionary...")
#     frequency_data = pd.read_excel('./python/NLT1.40_freq_list.xlsx', names=['レマ', '品詞', '読み', '頻度'])
    frequency_data = pd.read_excel(freq_path, names=['レマ', '品詞', '読み', '頻度'])
    frequency_dict = dict(zip(frequency_data['レマ'], frequency_data['頻度']))

    text = preprocess_text(text)

    # Построение полного пути к dicdir
    unidic_path = os.path.join(home_dir, "myenv/lib/python3.10/site-packages/unidic/dicdir")

    # Инициализация MeCab с использованием unidic
    tagger = MeCab.Tagger(r'-d "{}"'.format(unidic_path))
    node = tagger.parseToNode(text)
    rare_words = []

    logging.info("Processing text...")
    while node:
        surface = node.surface
        features = node.feature.split(',')
        if len(surface) == 0:
            node = node.next
            continue
        if len(features) >= 7:
            base_form = features[7] if features[6] != '*' else surface
        else:
            base_form = surface

        if is_valid_base_form(base_form):
            # Проверка на наличие в частотном словаре
            if base_form not in frequency_dict:
                node = node.next
                continue

            value = frequency_dict[base_form]

            if value < TRESHOLD:
                if features[0] not in ['助詞', '助動詞', '接続詞', '記号', '補助記号', '代名詞', '副詞', '連体詞', '感動詞']:
                    rare_words.append(base_form)
                else:
                    logging.debug(f"Excluded due to part of speech: {features[0]}")

        node = node.next

    logging.info(f"Rare words: {set(rare_words)}")

    # Запись результатов в файл
    #output_path = './temp/analyzed_text.txt'

    # Создаем директорию, если она не существует
    os.makedirs(os.path.dirname(output_path), exist_ok=True)

    with open(output_path, "w", encoding="utf-8") as f:
        f.write("Rare Words:\n")
        f.write(";".join(rare_words) + "\n")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Process some text.")
    parser.add_argument('text', type=str, help='Text to process')

    args = parser.parse_args()
    main(args.text)
