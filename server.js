const express = require('express');
const HybridModel = require('./hybridModel.js');

const app = express();
app.use(express.json());

const hybridModel = new HybridModel();

hybridModel.initialize().then(() => {
    console.log('HybridModel initialized');
});

let spinHistory = [];

app.post('/api/spin', async (req, res) => {
    try {
        const { number } = req.body;
        if (number < 0 || number > 36) {
            return res.status(400).json({ error: 'Invalid number (must be 0-36)' });
        }

        spinHistory.push(number);
        await hybridModel.train(spinHistory);

        const lastTwo = spinHistory.slice(-2);
        if (lastTwo.length < 2) {
            return res.status(400).json({ error: 'At least two numbers are required for prediction' });
        }

        const predictions = await hybridModel.predict(lastTwo[0], lastTwo[1]);
        res.json(predictions);
    } catch (error) {
        console.error('Error in /api/spin:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});
