SHELL := /bin/sh
ENGINE := xelatex
OUT_DIR := ../../output/pdf
JOB_NAME := warehouse-android-technical-specification

.PHONY: pdf clean

pdf:
	mkdir -p $(OUT_DIR)
	$(ENGINE) -interaction=nonstopmode -halt-on-error -jobname=$(JOB_NAME) -output-directory=$(OUT_DIR) main.tex
	$(ENGINE) -interaction=nonstopmode -halt-on-error -jobname=$(JOB_NAME) -output-directory=$(OUT_DIR) main.tex

clean:
	rm -f $(OUT_DIR)/$(JOB_NAME).aux $(OUT_DIR)/$(JOB_NAME).log $(OUT_DIR)/$(JOB_NAME).out $(OUT_DIR)/$(JOB_NAME).toc
