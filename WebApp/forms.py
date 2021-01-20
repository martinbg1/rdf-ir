from flask_wtf import FlaskForm
from wtforms import RadioField, SubmitField
from wtforms.validators import InputRequired

class RadioForm(FlaskForm):
    score = RadioField('Relevance', [InputRequired()], choices=[('0', 'Not relevant'),('1', 'Somewhat irrelevant'),('2', 'Somewhat relevant'),('3','Relevant')])
    submit = SubmitField("Submit")
    