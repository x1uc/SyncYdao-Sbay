import express from 'express'
import bodyParser from 'body-parser'
import {decode} from './ShanbayApiUtils.js'

const app = express()
const port = 3000

app.use(bodyParser.urlencoded({extended: false}))
app.use(bodyParser.json({limit: '50mb'}));
// app.use(bodyParser.urlencoded({limit: '50mb', extended: true}));
app.post('/', (req, res) => {
    console.log(req.body);
    let word = req.body.word
    if (word) {
        // 这里你可以对word进行处理，比如生成一个ID
        // 这个示例只是返回了相同的word作为ID
        let wordId = decode(word);
        console.log("---")
        console.log(wordId.id);
        console.log(wordId.word);
        var wordItem = {
            wordId: wordId.id,
            word: wordId.word
        }
        console.log(wordItem)
        res.status(200).send(wordItem);
    } else {
        res.status(400).send('No word provided');
    }
});

app.listen(port)
