from flask_wtf import FlaskForm
from wtforms import RadioField, SubmitField

class RadioForm(FlaskForm):
    score = RadioField('Relevance', choices=[('0', 'Not relevant'),('1', 'Somewhat irrelevant'),('2', 'Somewhat relevant'),('3','Relevant')])
    #submit = SubmitField("Submit")
    